package idevcod.score;

public class TestEvent {
    static final int SUCCESS = 0;
    static final int FAILED = -1;

    private String fileName;
    private int result;

    public TestEvent(String fileName, int result) {
        this.fileName = fileName;
        this.result = result;
    }

    String getFileName() {
        return fileName;
    }

    int getResult() {
        return result;
    }
}
