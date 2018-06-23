package idevcod.exam;

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

    private RunTestListener listener;

    private String zipFileName;

    public ExamLogger(String fullBuildResultPath, String zipFileName, int msgLevel, RunTestListener listener) throws FileNotFoundException {
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
            listener.notifyListener(new TestEvent(zipFileName, TestEvent.FAILED));
        } else {
            listener.notifyListener(new TestEvent(zipFileName, TestEvent.SUCCESS));
        }

        super.buildFinished(buildEvent);

        printStream.close();
    }
}
