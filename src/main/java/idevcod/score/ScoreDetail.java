package idevcod.score;

import java.util.HashMap;
import java.util.Map;

public class ScoreDetail {
    private String examPaperName;

    private int score;

    private Map<String, Integer> scoreMap = new HashMap<>();

    ScoreDetail(String examPaperName) {
        this.examPaperName = examPaperName;
    }

    String getExamPaperName() {
        return examPaperName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    Map<String, Integer> getScoreMap() {
        return scoreMap;
    }

    void addScoreItem(String className, String name, int score) {
        scoreMap.put(key(className, name), score);
    }

    private String key(String className, String name) {
        return className + "#" + name;
    }
}
