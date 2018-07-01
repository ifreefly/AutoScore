package idevcod.score;

import idevcod.util.FileUtil;
import idevcod.util.RarUtil;
import idevcod.util.ZipUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoScore {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScore.class);

    private static final String TARGET = "run_test";

    private static final String ZIP_POSTFIX = ".zip";

    private static final String RAR_POSTFIX = ".rar";

    private static final String TXT_POSTFIX = ".txt";

    private static final String ENCODING = "UTF-8";

    private SummaryCollector collector;

    private Map<String, Integer> scoreMap = new HashMap<>();

    private WorkPath workPath;

    public AutoScore(String workRootDir) {
        workPath = new WorkPath(workRootDir);
    }

    private void doScoring() throws IOException {
        workPath.prepareDir();
        loadScoreConfig(workPath.getConfPath());
        scoreAll(workPath.getInputDirPath(), workPath.getSummaryDirPath(), workPath.getTmpPath());
    }

    private void loadScoreConfig(String confPath) {
        String scoreConfigPath = confPath + File.separator + "scoreConfig.xml";
        File file = new File(scoreConfigPath);
        if (!file.exists()) {
            throw new IllegalStateException("scoreConfig" + scoreConfigPath + " not found");
        }

        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(file);
            Element root = document.getRootElement();

            for (Iterator<Element> iterator = root.elementIterator("score"); iterator.hasNext(); ) {
                Element element = iterator.next();
                Score score = new Score(element.attributeValue("className"), element.attributeValue("method"),
                        Integer.valueOf(element.attributeValue("score")));
                scoreMap.put(key(score.getClassName(), score.getName()), score.getScore());
            }
        } catch (DocumentException e) {
            LOGGER.error("parse scoreConfig {} failed", scoreConfigPath, e);
            throw new IllegalStateException("parse scoreConfig " + scoreConfigPath + "failed");
        }
    }

    private String key(String className, String name) {
        return className + "#" + name;
    }

    private void scoreAll(String inputDirPath, String summaryDirPath, String tmpPath) {
        File[] files = new File(inputDirPath).listFiles();

        collector = new SummaryCollector(files.length, summaryDirPath + File.separator + "runSummary.log");

        String desBuildFilePath = tmpPath + File.separator + "build.xml";

        for (File file : files) {
            if (!score(workPath.getTmpPath(), desBuildFilePath, file)) {
                collector.collectResult(new ScoreEvent(file.getName(), ScoreEvent.FAILED, "score failed"));
            }
        }

        collector.close();
    }

    private boolean score(String tmpPath, String desTemplateBuildFilePath, File file) {
        String fullFileName = file.getName();

        if (!prepareTmpDir(workPath.getTmpPath())) {
            return false;
        }

        try {
            if (!extractFile(file, workPath.getTmpPath())) {
                LOGGER.error("score {} failed, extract exam failed.", fullFileName);
                return false;
            }

            if (!copyTemplateBuildFile(workPath.getTemplatePath(), desTemplateBuildFilePath)) {
                LOGGER.error("score {} failed, copyTemplateBuildFile failed", fullFileName);
                return false;
            }

            if (!validateDirAndMkdirIfNeed(tmpPath)) {
                LOGGER.error("score {} failed, validate dir failed", fullFileName);
                return false;
            }

            copyUsecase(workPath.getUseCasePath(), workPath.getTmpPath());

            String fullBuildResultPath = workPath.getRunlogPath() + File.separator + fullFileName.substring(0, fullFileName.length() - 4) + TXT_POSTFIX;
            antRun(fullBuildResultPath, fullFileName, desTemplateBuildFilePath, TARGET);
        } catch (Exception e) {
            LOGGER.error("score {} failed, error is ", fullFileName, e);
            return false;
        }

        return true;
    }

    private boolean prepareTmpDir(String tmpPath) {
        if (!FileUtil.deleteDir(tmpPath)) {
            LOGGER.error("score {} failed, delete tmp path failed", tmpPath);
            return false;
        }

        if (!createDir(tmpPath)) {
            LOGGER.error("score {} failed, create tmp path failed", tmpPath);
            return false;
        }

        return true;
    }

    private boolean extractFile(File file, String desPath) throws IOException {
        String fullFileName = file.getName();
        if (fullFileName.endsWith(ZIP_POSTFIX)) {
            return ZipUtil.unzip(file.getCanonicalPath(), desPath);
        }

        if (fullFileName.endsWith(RAR_POSTFIX)) {
            return RarUtil.unrarFile(file.getCanonicalPath(), desPath);
        }

        LOGGER.error("score {} failed, postfix is not zip or rar!", fullFileName);

        return true;
    }

    private boolean validateDirAndMkdirIfNeed(String codePath) {
        if (!validateLibPath(codePath)) {
            return false;
        }

        if (!validateTestPath(codePath)) {
            return false;
        }

        return true;
    }

    private boolean validateTestPath(String codePath) {
        File testDirectory = new File(codePath + File.separator + "test");
        if (!testDirectory.exists()) {
            return true;
        }

        if (testDirectory.isDirectory()) {
            if (!FileUtil.deleteDir(testDirectory)) {
                LOGGER.warn("delete test dir failed");
                return false;
            }

            return true;
        }

        if (!testDirectory.delete()) {
            LOGGER.warn("delete test file failed");
            return false;
        }

        return true;
    }

    private boolean validateLibPath(String codePath) {
        File libDirectory = new File(codePath + File.separator + "lib");
        if (libDirectory.isFile()) {
            LOGGER.warn("lib path is a file");
            return false;
        }

        if (libDirectory.exists()) {
            return true;
        }

        if (libDirectory.mkdir()) {
            return true;
        }

        LOGGER.warn("validateLibPath failed");

        return false;
    }

    private void copyUsecase(String useCasePath, String tmpPath) throws IOException {
        FileUtil.copyDirectory(useCasePath, tmpPath);
    }

    public void antRun(String fullBuildResultPath, String fullFileName, String buildFilePath, String target) {
        File buildFile = new File(buildFilePath);

        Project project = new Project();

        ScoreLogger consoleLogger;
        try {
            consoleLogger = new ScoreLogger(fullBuildResultPath, fullFileName, Project.MSG_INFO, event -> {
                if (event.getResult() != TestEvent.SUCCESS) {
                    LOGGER.error("score {} result is {}", event.getZipName(), event.getResult());
                    collector.collectResult(new ScoreEvent(event.getZipName(), ScoreEvent.FAILED, "score failed"));
                } else {
                    if (!analyseResult(event, workPath.getCaseResultDirPath(), workPath.getScoreResultPath(), workPath.getSummaryDirPath(), workPath.getTmpPath())) {
                        LOGGER.error("score {} failed, analyseResult failed", event.getZipName());
                        collector.collectResult(new ScoreEvent(event.getZipName(), ScoreEvent.FAILED, "analyse result failed"));
                    } else {
                        collector.collectResult(new ScoreEvent(event.getZipName(), ScoreEvent.SUCCESS, "analyse result success"));
                    }
                }
            });
        } catch (FileNotFoundException e) {
            LOGGER.error("score {} failed, create buildListener failed.", fullFileName, e);
            return;
        }

        project.addBuildListener(consoleLogger);
        try {
            project.fireBuildStarted();
            project.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            helper.parse(project, buildFile);
            project.executeTarget(target);
            project.fireBuildFinished(null);
        } catch (BuildException e) {
            project.fireBuildFinished(e);
        }
    }

    private boolean analyseResult(TestEvent event, String caseResultDirPath, String scoreResultPath, String summaryDirPath, String tmpPath) {
        File desReport = new File(caseResultDirPath + File.separator + event.getZipName() + ".xml");
        if (!collectResult(desReport, tmpPath)) {
            LOGGER.error("score {} failed, copy report file failed", event.getZipName(), event.getResult());
            return false;
        }

        File scoreResultFile = new File(scoreResultPath + File.separator + event.getZipName() + ".csv");
        ScoreSummary summary = scoreExam(event.getZipName(), scoreResultFile, desReport);
        if (summary == null) {
            LOGGER.error("score {} failed, score exam failed", event.getZipName());
            return false;
        }

        File summaryFile = new File(summaryDirPath + File.separator + "summary" + ".csv");
        try (FileOutputStream fileOutputStream = new FileOutputStream(summaryFile, true);
             PrintStream printStream = new PrintStream(fileOutputStream, true, ENCODING)) {
            writeBom(printStream);

            printStream.println(summary.toString());
        } catch (IOException e) {
            LOGGER.info("score {} failed, write summary failed", event.getZipName());
            return false;
        }

        LOGGER.info("score {} success", event.getZipName());

        return true;
    }

    /**解决excel csv乱码的问题*/
    private void writeBom(PrintStream printStream) throws IOException {
        byte[] uft8bom={(byte)0xef,(byte)0xbb,(byte)0xbf};
        printStream.write(uft8bom);
    }


    private boolean collectResult(File desReport, String tmpPath) {
        File srcResultDirs = new File(tmpPath + File.separator + "build" + File.separator + "report");

        File[] files = srcResultDirs.listFiles();
        if (files == null) {
            return false;
        }

        Document desDocument = DocumentHelper.createDocument();
        Element root = desDocument.addElement("testsuites");

        for (File file : files) {
            SAXReader reader = new SAXReader();
            reader.setEncoding(ENCODING);
            try {
                Document document = reader.read(file);
                root.add(document.getRootElement());
            } catch (DocumentException e) {
                LOGGER.error("read {} xml failed", file.getName());
                return false;
            }
        }

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(ENCODING);
        try (FileOutputStream fileWriter = new FileOutputStream(desReport)) {
            XMLWriter xmlWriter = new XMLWriter(fileWriter, format);
            xmlWriter.write(desDocument);
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private ScoreSummary scoreExam(String examPaperName, File scoreResult, File desReport) {
        SAXReader reader = new SAXReader();
        reader.setEncoding(ENCODING);

        ScoreSummary scoreSummary = new ScoreSummary(examPaperName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(scoreResult);
             PrintStream printStream = new PrintStream(fileOutputStream, true, ENCODING)) {
            Document document = reader.read(desReport);

            for (Iterator<Element> testsuitIterator = document.getRootElement().elementIterator("testsuite");
                 testsuitIterator.hasNext(); ) {
                Element testsuit = testsuitIterator.next();
                int errors = Integer.parseInt(testsuit.attributeValue("errors"));
                int failures = Integer.parseInt(testsuit.attributeValue("failures"));
                int skipped = Integer.parseInt(testsuit.attributeValue("skipped"));
                int totalCase = Integer.parseInt(testsuit.attributeValue("tests"));

                scoreSummary.addSuccess(totalCase - errors - failures - skipped);
                scoreSummary.addError(errors);
                scoreSummary.addFailure(failures);
                scoreSummary.addSkipped(skipped);
                scoreSummary.addTotalCase(totalCase);

                for (Iterator<Element> testcaseIterator = testsuit.elementIterator("testcase"); testcaseIterator.hasNext(); ) {
                    Element testcase = testcaseIterator.next();
                    String className = testcase.attributeValue("classname");
                    String name = testcase.attributeValue("name");

                    String status = "success";
                    int caseScore = 0;
                    if (testcase.element("error") != null) {
                        status = "error";
                    } else if (testcase.element("failure") != null) {
                        status = "failure";
                    } else {
                        String key = key(className, name);
                        if (!scoreMap.containsKey(key)) {
                            LOGGER.error("score failed, test case {} not found score", key);
                            throw new IllegalStateException("score failed, test case " + key + " not found score");
                        }

                        caseScore = scoreMap.get(key(className, name));
                        scoreSummary.addScore(caseScore);
                    }

                    printStream.println(className + ", " + name + ", " + status + ", " + caseScore);
                }

            }

            printStream.println(scoreSummary.toString());
        } catch (DocumentException e) {
            LOGGER.error("read {} xml failed", desReport.getName(), e);
            return null;
        } catch (IOException e) {
            LOGGER.error("score {} failed", scoreResult.getName(), e);
            return null;
        }

        return scoreSummary;
    }

    private boolean copyTemplateBuildFile(String templatePath, String buildFilePath) {
        File srcBuildFile = new File(templatePath + File.separator + "build.xml");
        File desBuildFile = new File(buildFilePath);

        try {
            Files.copy(srcBuildFile.toPath(), desBuildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("copy build File failed.", e);
            return false;
        }

        return true;
    }

    private static String getWorkRootDir() {
        String workRootDir = System.getProperty("workPath");
        if (workRootDir != null) {
            return workRootDir;
        }

        try {
            return new File(AutoScore.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI()).getParentFile().getParentFile().getCanonicalPath();
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("get work root dir failed.", e);
        }

        return "null";
    }

    private boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        return dir.mkdirs();
    }

    public static void main(String[] args) {
        try {
            LOGGER.info("run");
//            String workRootDir = getWorkRootDir();
//            if (workRootDir == null) {
//                throw new IllegalStateException("workRootDir not found!");
//            }

            String workRootDir = "E:\\tmp\\debug1";
            LOGGER.info("workRootDir is {}", workRootDir);
            AutoScore exam = new AutoScore(workRootDir);
            exam.doScoring();
        } catch (Throwable t) {
            LOGGER.error("score failed, throwable is ", t);
        }
    }
}

