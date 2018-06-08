package exam;

public class Score {
    private String className;

    private String name;

    private int score;

    public Score(String className, String name, int score) {
        this.className = className;
        this.name = name;
        this.score = score;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
