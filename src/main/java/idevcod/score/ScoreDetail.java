package idevcod.score;

import java.util.HashMap;
import java.util.Map;

public class ScoreDetail {
    private String examPaperName;

    private int score;

    private Map<String, Integer> scoreMap = new HashMap<>();

    public ScoreDetail(String examPaperName) {
        this.examPaperName = examPaperName;
    }

    public String getExamPaperName() {
        return examPaperName;
    }

    public void setExamPaperName(String examPaperName) {
        this.examPaperName = examPaperName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<String, Integer> getScoreMap() {
        return scoreMap;
    }

    public void addScoreItem(String className, String name, int score) {
        scoreMap.put(key(className, name), score);
    }

    private String key(String className, String name) {
        return className + "#" + name;
    }
}
