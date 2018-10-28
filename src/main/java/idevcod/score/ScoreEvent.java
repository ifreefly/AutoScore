package idevcod.score;

public class ScoreEvent {
    static final int SUCCESS = 0;
    static final int FAILED = -1;

    private String fileName;
    private int result;
    private String description;

    public ScoreEvent(String fileName, int result, String description) {
        this.fileName = fileName;
        this.result = result;
        this.description = description;
    }

    int getResult() {
        return result;
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
