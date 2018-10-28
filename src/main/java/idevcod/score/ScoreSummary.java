package idevcod.score;

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

    void addSuccess(int success) {
        this.success += success;
    }

    void addFailure(int failure) {
        this.failure += failure;
    }

    void addError(int error) {
        this.error += error;
    }

    void addSkipped(int skipped) {
        this.skipped += skipped;
    }

    void addTotalCase(int totalCase) {
        this.totalCase += totalCase;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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
