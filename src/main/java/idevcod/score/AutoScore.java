package idevcod.score;

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
import java.util.Iterator;

public class AutoScore {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScore.class);

    private static final String TARGET = "run_test";

    private static final String ENCODING = "UTF-8";

    private SummaryCollector summaryCollector;

    private DetailCollector detailCollector;

    private WorkPath workPath;

    private ScoreConfig scoreConfig;

    public AutoScore(String workRootDir) {
        workPath = new WorkPath(workRootDir);
        scoreConfig = new ScoreConfig(workPath.getConfPath());
    }

    private void doScoring() throws IOException {
        scoreConfig.loadScoreConfig();
        workPath.prepareDir();
        scoreAll(workPath.getInputDirPath());
    }

    private void scoreAll(String inputDirPath) {
        File[] files = new File(inputDirPath).listFiles();
        if (files == null) {
            throw new IllegalStateException("get exam paper failed!");
        }

        summaryCollector = new SummaryCollector(files.length, workPath.getSummaryLogPath());
        detailCollector = new DetailCollector(workPath.getDetailResultPath(), scoreConfig);

        for (File file : files) {
            if (!score(file)) { // 打分后还有结果分析，需要结果分析成功后，才能算真正的success，因此此处只收集打分失败的结果。
                summaryCollector.collectResult(new ScoreEvent(file.getName(), ScoreEvent.FAILED, "score failed"));
            }
        }

        summaryCollector.close();
        detailCollector.close();
    }

    private boolean score(File examFile) {
        String paperName = examFile.getName();

        try {
            if (!workPath.prepareTmpWorkDir(examFile)) {
                LOGGER.error("score {} failed", paperName);
                return false;
            }

            antRun(workPath.getBuildResultPath(paperName), paperName, workPath.getWorkBuildFilePath(), TARGET);
        } catch (Exception e) {
            LOGGER.error("score {} failed, error is ", paperName, e);
            return false;
        }

        return true;
    }


    private void antRun(String fullBuildResultPath, String paperName, String buildFilePath, String target) {
        File buildFile = new File(buildFilePath);

        Project project = new Project();

        ScoreLogger consoleLogger;
        try {
            consoleLogger = new ScoreLogger(fullBuildResultPath, paperName, Project.MSG_INFO, event -> {
                if (event.getResult() != TestEvent.SUCCESS) {
                    LOGGER.error("score {} result is {}", event.getFileName(), event.getResult());
                    summaryCollector.collectResult(new ScoreEvent(event.getFileName(), ScoreEvent.FAILED, "score failed"));
                } else {
                    if (!analyseResult(event, workPath)) {
                        LOGGER.error("score {} failed, analyseResult failed", event.getFileName());
                        summaryCollector.collectResult(new ScoreEvent(event.getFileName(), ScoreEvent.FAILED, "analyse result failed"));
                    } else {
                        LOGGER.info("score {} success", event.getFileName());
                        summaryCollector.collectResult(new ScoreEvent(event.getFileName(), ScoreEvent.SUCCESS, "analyse result success"));
                    }
                }
            });
        } catch (FileNotFoundException e) {
            LOGGER.error("score {} failed, create buildListener failed.", paperName, e);
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


    private boolean analyseResult(TestEvent event, WorkPath workPath) {
        File desReport = new File(workPath.getReportFilePath(event.getFileName()));
        if (!collectResult(desReport, workPath.getTmpPath())) {
            LOGGER.error("copy {} report file failed", event.getFileName());
            return false;
        }

        ScoreResult scoreResult = scoreExam(event.getFileName(), workPath.getScoreResultPath(event.getFileName()), desReport);
        if (scoreResult == null) {
            LOGGER.error("score {} failed", event.getFileName());
            return false;
        }

        File summaryFile = new File(workPath.getSummaryResultPath());
        try (FileOutputStream fileOutputStream = new FileOutputStream(summaryFile, true);
             PrintStream printStream = new PrintStream(fileOutputStream, true, ENCODING)) {
            writeBom(printStream);

            printStream.println(scoreResult.getScoreSummary().toString());

            detailCollector.collectResult(scoreResult.getScoreDetail());
        } catch (IOException e) {
            LOGGER.info("write {} summary failed, exception is ", event.getFileName(), e);
            return false;
        }

        return true;
    }

    /**
     * 解决excel csv乱码的问题
     */
    private void writeBom(PrintStream printStream) throws IOException {
        byte[] uft8bom = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
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


    private ScoreResult scoreExam(String examPaperName, String scoreResultPath, File desReport) {
        SAXReader reader = new SAXReader();
        reader.setEncoding(ENCODING);

        ScoreSummary scoreSummary = new ScoreSummary(examPaperName);
        ScoreDetail scoreDetail = new ScoreDetail(examPaperName);
        ScoreResult scoreResult = new ScoreResult(scoreSummary, scoreDetail);

        File scoreResultFile = new File(scoreResultPath);
        try (FileOutputStream fileOutputStream = new FileOutputStream(scoreResultFile);
             PrintStream printStream = new PrintStream(fileOutputStream, true, ENCODING)) {
            writeBom(printStream);

            int score = 0;

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
                        caseScore = scoreConfig.getScore(className, name);
                    }

                    score += caseScore;
                    scoreDetail.addScoreItem(className, name, caseScore);

                    printStream.println(className + ", " + name + ", " + status + ", " + caseScore);
                }
            }

            scoreSummary.setScore(score);
            scoreDetail.setScore(score);

            printStream.println(scoreSummary.toString());
        } catch (DocumentException e) {
            LOGGER.error("read {} xml failed", desReport.getName(), e);
            return null;
        } catch (IOException e) {
            LOGGER.error("score {} failed", scoreResultFile.getName(), e);
            return null;
        }

        return scoreResult;
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

    public static void main(String[] args) {
        try {
            LOGGER.info("run");
            String workRootDir = getWorkRootDir();
            if (workRootDir == null) {
                throw new IllegalStateException("workRootDir not found!");
            }

            LOGGER.info("workRootDir is {}", workRootDir);
            AutoScore exam = new AutoScore(workRootDir);
            exam.doScoring();
        } catch (Throwable t) {
            LOGGER.error("score failed, throwable is ", t);
        }
    }
}

