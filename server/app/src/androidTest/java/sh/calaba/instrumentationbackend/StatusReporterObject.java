package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StatusReporterObject {

    public static void report(Context context,
                              String method,
                              String message,
                              StatusReporter.FinishedState extraState,
                              boolean hasData) {


        System.out.println("Failure file: " + getOutputFile(FAILURE_FILE_PATH, context));
        System.out.println("Finished file: " + getOutputFile(FINISHED_FILE_PATH, context));


        if (hasData) {

            System.out.println("method: " + method);

            if ("report-failure".equals(method)) {
                try {
                    reportFailure(message, context);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ("clear".equals(method)) {
                clear(context);
            } else if ("report-finished".equals(method)) {
                try {
                    reportFinished(extraState, context);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("No such method '" + method + "'");
            }

        } else {
            throw new RuntimeException("No data sent");
        }

        // TODO: Check if it works
        if (context instanceof Activity){
            ((Activity) context).finish();
        }
    }

    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_METHOD = "method";
    public static final String EXTRA_STATE = "state";

    private static final String FAILURE_FILE_PATH = "calabash_failure.out";
    private static final String FINISHED_FILE_PATH = "calabash_finished.out";

    private static void reportFailure(String message, Context context) throws IOException {
        System.out.println("Failure state: " + message);

        dumpPublicFile(FAILURE_FILE_PATH, message, context);
    }

    private static void clear(Context context) {
        clearFailure(context);
        clearFinishedStatus(context);
    }

    private static void clearFailure(Context context) {
        getOutputFile(FAILURE_FILE_PATH, context).delete();
    }

    private static void clearFinishedStatus(Context context) {
        getOutputFile(FINISHED_FILE_PATH, context).delete();
    }

    private static void reportFinished(StatusReporter.FinishedState finishedState,
                                       Context context) throws IOException {
        System.out.println("Finished state: " + finishedState.toString());

        dumpPublicFile(FINISHED_FILE_PATH, finishedState.toString(), context);
    }

    private static void dumpPublicFile(String path, String content,
                                       Context context) throws IOException {
        File outputFile = getOutputFile(path, context);
        try (OutputStream fileOutputStream = new FileOutputStream(outputFile, false)) {
            fileOutputStream.write(content.getBytes());
        }

        if (!outputFile.setReadable(true, false)) {
            System.err.println("WARNING: Failed to make file " + outputFile.getAbsolutePath() + " readable!");
        }
    }

    private static File getOutputFile(String path, Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        return new File(contextWrapper.getFilesDir(), path);
    }
}
