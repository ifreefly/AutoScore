package idevcod.score;

public class ScoreResult {
    private ScoreSummary scoreSummary;

    private ScoreDetail scoreDetail;

    public ScoreResult(ScoreSummary scoreSummary, ScoreDetail scoreDetail) {
        this.scoreSummary = scoreSummary;
        this.scoreDetail = scoreDetail;
    }

    public ScoreSummary getScoreSummary() {
        return scoreSummary;
    }

    public ScoreDetail getScoreDetail() {
        return scoreDetail;
    }
}
