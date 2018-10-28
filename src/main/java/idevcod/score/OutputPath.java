package idevcod.score;

import java.io.File;

class OutputPath {

    private static final String TXT_POSTFIX = ".txt";

    private String resultPath;

    private String caseResultDirPath;

    private String runlogPath;

    private String scoreResultPath;

    OutputPath(String outputDir) {
        resultPath = outputDir + File.separator + "result";

        String runDir = outputDir + File.separator + "run";
        runlogPath = runDir + File.separator + "runlog";
        caseResultDirPath = runDir + File.separator + "caseResult";
        scoreResultPath = runDir + File.separator + "score";
    }

    void createOutputStructure() {
        createDir(resultPath);
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
        return resultPath + File.separator + "runSummary.log";
    }

    String getReportPath(String fileName) {
        return caseResultDirPath + File.separator + fileName + ".xml";
    }

    String getScoreResultPath(String fileName) {
        return scoreResultPath + File.separator + fileName + ".csv";
    }

    String getSummaryResultPath() {
        return resultPath + File.separator + "summary.csv";
    }

    String getDetailResultPath() {
        return resultPath + File.separator + "detail.csv";
    }
}
