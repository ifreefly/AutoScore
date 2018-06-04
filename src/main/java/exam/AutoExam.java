package exam;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtil;
import util.TimeUtil;
import util.ZipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

public class AutoExam {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoExam.class);

    private static final String BUILD_FILE = "E:\\tmp\\debug\\mydebug\\build.xml";

    private static final String TARGET = "run_test";

    private static final String ZIP_POSTFIX = ".zip";

    private static final String TXT_POSTFIX = ".txt";

    private String inputDirPath;

    private String tmpPath;

    private String templatePath;

    private String usceCasePath;

    private String outPutDir;

    private String summaryDirPath;

    private String reportDirPath;

    private String buildResultPath;

    public AutoExam() {
        this(".");
    }

    public AutoExam(String workRootDir) {
        this.inputDirPath = workRootDir + File.separator + "input";
        this.tmpPath = workRootDir + File.separator + "tmp";
        this.templatePath = workRootDir + File.separator + "template";
        this.usceCasePath = workRootDir + File.separator + "usecase";
        this.outPutDir = workRootDir + File.separator + "output";
    }

    public void doScoring() {
        prepare();
//        System.out.println(FileUtil.deleteDir(tmpPath));
        scoreAll();
    }

    public void prepare() {
        File workFile = new File(outPutDir);
        if (workFile.isFile()) {
            throw new IllegalStateException(outPutDir + " is a file!");
        }

        if (!workFile.exists()) {
            workFile.mkdirs();
        }

        String workDir = outPutDir + File.separator + TimeUtil.getTimeStamp(System.currentTimeMillis());

        summaryDirPath = workDir + File.separator + "summary";
        reportDirPath = workDir + File.separator + "report";
        buildResultPath = workDir + File.separator + "result";

        createDir(summaryDirPath);
        createDir(reportDirPath);
        createDir(buildResultPath);
        createDir(tmpPath);
    }

    public void scoreAll() {
        File inputFileDir = new File(inputDirPath);
        if (!inputFileDir.exists()) {
            inputFileDir.mkdirs();
            throw new IllegalStateException("input dir not found and system create it!");
        }

        if (!inputFileDir.isDirectory()) {
            throw new IllegalStateException("input dir not exist and it's a file!");
        }

        File[] files = inputFileDir.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalStateException("no exam zip found");
        }


        String buildFilePath = tmpPath + File.separator + " build.xml";

        for (File file : files) {
            String fullFileName = file.getName();

            if (!FileUtil.deleteDir(tmpPath)) {
                LOGGER.error("score {} failed, delete tmp path failed", fullFileName);
                continue;
            }

            createDir(tmpPath);

            try {
                if (!fullFileName.endsWith(ZIP_POSTFIX)) {
                    LOGGER.error("score {} failed, postfix is not zip", fullFileName);
                    continue;
                }

                ZipUtil.unzip(file, tmpPath);
                if (!copyBuildFile(buildFilePath)) {
                    LOGGER.error("score {} failed, copyBuildFile failed", fullFileName);
                    continue;
                }

                copyUsecase();

                String fullBuildResultPath = buildResultPath + File.separator + fullFileName.substring(0, fullFileName.length() - 4) + TXT_POSTFIX;
                antRun(fullBuildResultPath, fullFileName, buildFilePath, TARGET);
            } catch (Exception e) {
                LOGGER.error("score {} failed, error is ", fullFileName, e);
            }
        }
    }

    private void copyUsecase() throws IOException {
        FileUtil.copyDirectory(usceCasePath, tmpPath);
    }

    public void antRun(String fullBuildResultPath, String fullFileName, String buildFilePath, String target) {
        File buildFile = new File(buildFilePath);

        Project project = new Project();

        ExamLogger consoleLogger;
        try {
            consoleLogger = new ExamLogger(fullBuildResultPath, fullFileName, Project.MSG_INFO, event -> {
                if (event.getResult() != ScoreEvent.SUCCESS) {
                    LOGGER.error("score {} result is {}", event.getZipName(), event.getResult());
                } else {
                    LOGGER.info("score {} success", event.getZipName());
//                    if (!copyReportFile(fullFileName)) {
//                        LOGGER.error("score {} failed, copy report file failed", event.getZipName(), event.getResult());
//                    } else {
//                        LOGGER.info("score {} success", event.getZipName());
//                    }
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

    private boolean copyReportFile(String fullFileName) {
        File srcReport = new File(tmpPath + File.separator + "build" + File.separator + "report" +
                File.separator + "html" + File.separator + "all-tests.html");
        File desReport = new File(outPutDir + File.separator + "report" + fullFileName + ".html");

//        try {
//            Files.copy(srcReport.toPath(), desReport.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        } catch (Exception e) {
//            LOGGER.error("copy {} report file failed", fullFileName, e);
//            return false;
//        }

        return true;
    }

    private boolean copyBuildFile(String buildFilePath) {
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

    private boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        return dir.mkdirs();
    }

    public static void main(String[] args) {
        LOGGER.info("run");
        AutoExam exam = new AutoExam("E:\\tmp\\debug");
        exam.doScoring();
//        exam.antRun(BUILD_FILE, TARGET);
    }
}
