package exam;

public class ScoreSummary {
    private String examPaperName;
    private int success;
    private int failure;
    private int error;
    private int skipped;
    private int totalCase;
    private int score;

    public ScoreSummary(String examPaperName) {
        this.examPaperName = examPaperName;
    }

    public String getExamPaperName() {
        return examPaperName;
    }

    public void setExamPaperName(String examPaperName) {
        this.examPaperName = examPaperName;
    }

    public int getSuccess() {
        return success;
    }

    public void addSuccess(int success) {
        this.success += success;
    }

    public int getFailure() {
        return failure;
    }

    public void addFailure(int failure) {
        this.failure += failure;
    }

    public int getError() {
        return error;
    }

    public void addError(int error) {
        this.error += error;
    }

    public int getSkipped() {
        return skipped;
    }

    public void addSkipped(int skipped) {
        this.skipped += skipped;
    }

    public int getTotalCase() {
        return totalCase;
    }

    public void addTotalCase(int totalCase) {
        this.totalCase += totalCase;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    @Override
    public String toString() {
        return "summary," + examPaperName +
                ", score=" + score +
                ", success=" + success +
                "; failure=" + failure +
                "; error=" + error +
                "; skipped=" + skipped +
                "; totalCase=" + totalCase;
    }
}
