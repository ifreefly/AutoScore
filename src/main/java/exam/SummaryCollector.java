package exam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class SummaryCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummaryCollector.class);

    private FileOutputStream fileOutputStream;

    private PrintStream printStream;

    public SummaryCollector(String collectorFilePath) {
        File collectFile = new File(collectorFilePath);
        if (collectFile.isDirectory()) {
            throw new IllegalStateException("collectorFilePath" + collectorFilePath + "is directory");
        }

        try {
            if (!collectFile.exists()) {
                collectFile.createNewFile();
            }

            fileOutputStream = new FileOutputStream(collectFile, true);
            printStream = new PrintStream(fileOutputStream, true, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void collectResult(ScoreEvent scoreEvent) {
        printStream.println(scoreEvent.toString());
    }

    public void close() {
        closeQuietly(printStream);
        closeQuietly(fileOutputStream);
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOGGER.error("close failed", e);
            }
        }
    }
}
