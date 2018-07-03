package idevcod.score;

public class ScoreEvent {
    public static final int SUCCESS = 0;
    public static final int FAILED = -1;

    private String fileName;
    private int result;
    private String description;

    public ScoreEvent(String fileName, int result, String description) {
        this.fileName = fileName;
        this.result = result;
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public int getResult() {
        return result;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ScoreEvent{" +
                "fileName='" + fileName + '\'' +
                ", result=" + result +
                ", description='" + description + '\'' +
                '}';
    }
}
