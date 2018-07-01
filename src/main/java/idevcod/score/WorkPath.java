package idevcod.score;

import idevcod.util.TimeUtil;

import java.io.File;
import java.io.IOException;

public class WorkPath {
    private String inputDirPath;

    private String confPath;

    private String tmpPath;

    private String templatePath;

    private String useCasePath;

    private String outPutDir;

    private String summaryDirPath;

    private String caseResultDirPath;

    private String runlogPath;

    private String scoreResultPath;

    public WorkPath(String workRootDir) {
        this.inputDirPath = workRootDir + File.separator + "input";
        this.confPath = workRootDir + File.separator + "conf";
        this.tmpPath = workRootDir + File.separator + "tmp";
        this.templatePath = this.confPath + File.separator + "template";
        this.useCasePath = workRootDir + File.separator + "usecase" + File.separator + "test";
        this.outPutDir = workRootDir + File.separator + "output";
    }

    public void prepareDir() throws IOException {
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

        String workDir = outPutDir + File.separator + TimeUtil.getTimeStamp(System.currentTimeMillis());

        summaryDirPath = workDir + File.separator + "summary";

        String runDir = workDir + File.separator + "run";
        runlogPath = runDir + File.separator + "runlog";
        caseResultDirPath = runDir + File.separator + "caseResult";
        scoreResultPath = runDir + File.separator + "score";

        createDir(summaryDirPath);
        createDir(caseResultDirPath);
        createDir(runlogPath);
        createDir(scoreResultPath);
        createDir(tmpPath);
    }

    private boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        return dir.mkdirs();
    }

    public String getInputDirPath() {
        return inputDirPath;
    }

    public String getConfPath() {
        return confPath;
    }

    public String getTmpPath() {
        return tmpPath;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public String getUseCasePath() {
        return useCasePath;
    }

    public String getOutPutDir() {
        return outPutDir;
    }

    public String getSummaryDirPath() {
        return summaryDirPath;
    }

    public String getCaseResultDirPath() {
        return caseResultDirPath;
    }

    public String getRunlogPath() {
        return runlogPath;
    }

    public String getScoreResultPath() {
        return scoreResultPath;
    }
}
