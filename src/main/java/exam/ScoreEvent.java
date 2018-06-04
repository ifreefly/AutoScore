package exam;

public class ScoreEvent {
    public static final int SUCCESS = 0;
    public static final int FAILED = -1;

    private String zipName;
    private int result;

    public ScoreEvent(String zipName, int result) {
        this.zipName = zipName;
        this.result = result;
    }

    public String getZipName() {
        return zipName;
    }

    public int getResult() {
        return result;
    }
}
