package idevcod.score;

class ScoreResult {
    private ScoreSummary scoreSummary;

    private ScoreDetail scoreDetail;

    ScoreResult(ScoreSummary scoreSummary, ScoreDetail scoreDetail) {
        this.scoreSummary = scoreSummary;
        this.scoreDetail = scoreDetail;
    }

    ScoreSummary getScoreSummary() {
        return scoreSummary;
    }

    ScoreDetail getScoreDetail() {
        return scoreDetail;
    }
}
