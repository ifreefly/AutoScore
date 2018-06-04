package exam;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class ExamLogger extends DefaultLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExamLogger.class);

    private PrintStream printStream;

    private ScoreListener listener;

    private String zipFileName;

    public ExamLogger(String fullBuildResultPath, String zipFileName, int msgLevel, ScoreListener listener) throws FileNotFoundException {
        printStream = new PrintStream(new FileOutputStream(new File(fullBuildResultPath)), true);

        this.listener = listener;
        this.zipFileName = zipFileName;

        this.setErrorPrintStream(printStream);
        this.setOutputPrintStream(printStream);
        this.setMessageOutputLevel(msgLevel);
    }

    @Override
    public void buildFinished(BuildEvent buildEvent) {
        if (buildEvent.getException() != null) {
            listener.notifyListener(new ScoreEvent(zipFileName, ScoreEvent.FAILED));
        } else {
            listener.notifyListener(new ScoreEvent(zipFileName, ScoreEvent.SUCCESS));
        }

        super.buildFinished(buildEvent);

        printStream.close();
    }
}
