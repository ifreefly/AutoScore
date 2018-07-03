package idevcod.score;

public class TestEvent {
    public static final int SUCCESS = 0;
    public static final int FAILED = -1;

    private String fileName;
    private int result;

    public TestEvent(String fileName, int result) {
        this.fileName = fileName;
        this.result = result;
    }

    public String getFileName() {
        return fileName;
    }

    public int getResult() {
        return result;
    }
}
