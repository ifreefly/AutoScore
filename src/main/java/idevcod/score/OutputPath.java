package idevcod.score;

import java.io.File;

public class OutputPath {

    private static final String TXT_POSTFIX = ".txt";

    private String outputDir;

    private String summaryDirPath;

    private String caseResultDirPath;

    private String runlogPath;

    private String scoreResultPath;

    public OutputPath(String outputDir) {
        this.outputDir = outputDir;
        summaryDirPath = outputDir + File.separator + "summary";

        String runDir = outputDir + File.separator + "run";
        runlogPath = runDir + File.separator + "runlog";
        caseResultDirPath = runDir + File.separator + "caseResult";
        scoreResultPath = runDir + File.separator + "score";
    }

    void createOutputStruct() {
        createDir(summaryDirPath);
        createDir(caseResultDirPath);
        createDir(runlogPath);
        createDir(scoreResultPath);
    }

    private boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        return dir.mkdirs();
    }

    String getBuildResultPath(String fileName) {
        return runlogPath + File.separator + fileName + TXT_POSTFIX;
    }

    String getSummaryLogPath() {
        return summaryDirPath + File.separator + "runSummary.log";
    }

    String getReportPath(String fileName) {
        return caseResultDirPath + File.separator + fileName + ".xml";
    }

    String getScoreResultPath(String fileName) {
        return scoreResultPath + File.separator + fileName + ".csv";
    }

    String getSummaryResultPath() {
        return summaryDirPath + File.separator + "summary" + ".csv";
    }
}
