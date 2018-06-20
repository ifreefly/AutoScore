package exam;

public class ScoreEvent {
    public static final int SUCCESS = 0;
    public static final int FAILED = -1;

    private String zipName;
    private int result;
    private String description;

    public ScoreEvent(String zipName, int result, String description) {
        this.zipName = zipName;
        this.result = result;
        this.description = description;
    }

    public String getZipName() {
        return zipName;
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
                "zipName='" + zipName + '\'' +
                ", result=" + result +
                ", description='" + description + '\'' +
                '}';
    }
}
