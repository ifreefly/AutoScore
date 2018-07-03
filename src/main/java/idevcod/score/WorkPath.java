package idevcod.score;

import idevcod.util.TimeUtil;

import java.io.File;
import java.io.IOException;

class WorkPath {

    private String inputDirPath;

    private String confPath;

    private TmpWorkPath tmpWorkPath;

    private String templateBuildFilePath;

    private String useCasePath;

    private String outPutDir;

    private OutputPath outputPath;

    WorkPath(String workRootDir) {
        this.inputDirPath = workRootDir + File.separator + "input";
        this.confPath = workRootDir + File.separator + "conf";
        this.templateBuildFilePath = this.confPath + File.separator + "template" + File.separator + "build.xml";
        this.useCasePath = workRootDir + File.separator + "usecase" + File.separator + "test";
        this.outPutDir = workRootDir + File.separator + "output";

        tmpWorkPath = new TmpWorkPath(workRootDir + File.separator + "tmp");
    }

    void prepareDir() throws IOException {
        validateProject();
        createDir();
    }

    private void validateProject() throws IOException {
        validateInput();
        validateTest();
    }

    private void validateInput() throws IOException {
        File inputDirFile = new File(inputDirPath);
        if (!inputDirFile.exists() && !inputDirFile.mkdirs()){
            throw new IllegalStateException("input dir not found and system create failed!");
        }

        if (!inputDirFile.isDirectory()) {
            throw new IllegalStateException("input dir not exist and it's a file!");
        }

        File[] files = inputDirFile.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalStateException("no exam zip found in " + inputDirFile.getCanonicalPath());
        }
    }

    private void validateTest() {
        File testDirFile = new File(useCasePath);
        if (!testDirFile.isDirectory()) {
            throw new IllegalStateException("useCasePath " + useCasePath + " not found!");
        }
    }

    private void createDir() {
        File workFile = new File(outPutDir);
        if (workFile.isFile()) {
            throw new IllegalStateException(outPutDir + " is a file!");
        }

        if (!workFile.exists()) {
            workFile.mkdirs();
        }

        outputPath = new OutputPath(outPutDir + File.separator + TimeUtil.getTimeStamp(System.currentTimeMillis()));

        outputPath.createOutputStruct();
    }

    boolean prepareTmpWorkDir(File examFile) throws IOException {
        return tmpWorkPath.prepareTmpWorkDir(this, examFile);
    }

    String getWorkBuildFilePath() {
        return tmpWorkPath.getBuildFilePath();
    }

    String getInputDirPath() {
        return inputDirPath;
    }

    String getConfPath() {
        return confPath;
    }

    String getTemplateBuildFilePath() {
        return templateBuildFilePath;
    }

    String getTmpPath() {
        return tmpWorkPath.getTmpWorkPath();
    }

    String getBuildResultPath(String fileName) {
       return outputPath.getBuildResultPath(fileName);
    }

    String getSummaryLogPath() {
        return outputPath.getSummaryLogPath();
    }

    String getReportFilePath(String fileName) {
        return outputPath.getReportPath(fileName);
    }

    String getScoreResultPath(String fileName) {
        return outputPath.getScoreResultPath(fileName);
    }

    String getSummaryResultPath() {
        return outputPath.getSummaryResultPath();
    }

    String getUseCasePath() {
        return useCasePath;
    }
}
