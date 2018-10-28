package idevcod.score;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

public class DetailCollector implements AutoCloseable{
    private static final Logger LOGGER = LoggerFactory.getLogger(DetailCollector.class);

    private ScoreConfig scoreConfig;

    private FileOutputStream fileOutputStream;

    private PrintStream printStream;

    public DetailCollector(String detailPath, ScoreConfig scoreConfig) {
        this.scoreConfig = scoreConfig;

        File detailFile = new File(detailPath);
        if (detailFile.isDirectory()) {
            throw new IllegalStateException("detailPath" + detailPath + "is directory");
        }

        try {
            if (!detailFile.exists()) {
                detailFile.createNewFile();
            }

            fileOutputStream = new FileOutputStream(detailFile, true);
            printStream = new PrintStream(fileOutputStream, true, "UTF-8");

            initHeader();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void initHeader() {
        StringBuilder sb = new StringBuilder("/, total");
        for (String useCase : scoreConfig.getScoreMap().keySet()) {
            sb.append(", ");
            sb.append(useCase);
        }

        printStream.println(sb.toString());
    }

    public void collectResult(ScoreDetail scoreDetail) {
        StringBuilder sb = new StringBuilder(scoreDetail.getExamPaperName());
        sb.append(", ").append(scoreDetail.getScore());

        Map<String, Integer> scoreMap = scoreDetail.getScoreMap();
        for (String useCase : scoreConfig.getScoreMap().keySet()) {
            sb.append(", ").append((int)scoreMap.get(useCase));
        }

        printStream.println(sb.toString());
    }

    @Override
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
