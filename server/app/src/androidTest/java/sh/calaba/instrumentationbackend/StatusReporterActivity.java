package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.content.ContextWrapper;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StatusReporterActivity extends Activity {
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_METHOD = "method";
    public static final String EXTRA_STATE = "state";

    private static final String FAILURE_FILE_PATH = "calabash_failure.out";
    private static final String FINISHED_FILE_PATH = "calabash_finished.out";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        {
            System.out.println("Failure file: "+ getOutputFile(FAILURE_FILE_PATH));
            System.out.println("Finished file: "+ getOutputFile(FINISHED_FILE_PATH));
        }

        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                Bundle extras = getIntent().getExtras();

                String method = extras.getString(EXTRA_METHOD);

                System.out.println("method: " + method);

                if ("report-failure".equals(method)) {
                    String message = extras.getString(EXTRA_MESSAGE);

                    try {
                        reportFailure(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if ("clear".equals(method)) {
                    clear();
                } else if ("report-finished".equals(method)) {
                    try {
                        reportFinished((StatusReporter.FinishedState) extras.get(EXTRA_STATE));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new RuntimeException("No such method '" + method + "'");
                }
            } else {
                throw new RuntimeException("No extras given");
            }
        } else {
            throw new RuntimeException("No intent given");
        }

        finish();
    }

    private void reportFailure(String message) throws IOException {
        System.out.println("Failure state: " + message);

        clearFailure();
        dumpPublicFile(FAILURE_FILE_PATH, message);
    }

    private void clear() {
        clearFailure();
        clearFinishedStatus();
    }

    private void clearFailure() {
        getOutputFile(FAILURE_FILE_PATH).delete();
    }

    private void clearFinishedStatus() {
        getOutputFile(FINISHED_FILE_PATH).delete();
    }

    private void reportFinished(StatusReporter.FinishedState finishedState) throws IOException {
        System.out.println("Finished state: " + finishedState.toString());

        dumpPublicFile(FINISHED_FILE_PATH, finishedState.toString());
    }

    private void dumpPublicFile(String path, String content) throws IOException {
        File outputFile = getOutputFile(path);
        try (OutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            fileOutputStream.write(content.getBytes());
        }

        if (!outputFile.setReadable(true, false)) {
            System.err.println("WARNING: Failed to make file " + outputFile.getAbsolutePath() + " readable!");
        }
    }

    private File getOutputFile(String path) {
        ContextWrapper contextWrapper = new ContextWrapper(this);
        return new File(contextWrapper.getFilesDir(), path);
    }
}
