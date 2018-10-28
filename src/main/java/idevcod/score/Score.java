package idevcod.score;

public class Score {
    private String className;

    private String name;

    private int score;

    public Score(String className, String name, int score) {
        this.className = className;
        this.name = name;
        this.score = score;
    }

    String getClassName() {
        return className;
    }

    String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
