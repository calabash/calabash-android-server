package sh.calaba.instrumentationbackend;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import sh.calaba.instrumentationbackend.StatusReporter.Method;
import static sh.calaba.instrumentationbackend.StatusReporter.REPORT_FAILURE_METHOD;
import static sh.calaba.instrumentationbackend.StatusReporter.REPORT_FINISHED_METHOD;

public class StatusReporterObject {

    private static final String FAILURE_FILE_PATH = "calabash_failure.out";
    private static final String FINISHED_FILE_PATH = "calabash_finished.out";

    public static void report(@Method String method,
                              String message,
                              StatusReporter.FinishedState extraState) {

        System.out.println("Failure file: " + getOutputFile(FAILURE_FILE_PATH));
        System.out.println("Finished file: " + getOutputFile(FINISHED_FILE_PATH));

        System.out.println("method: " + method);

        if (REPORT_FAILURE_METHOD.equals(method)) {
            try {
                reportFailure(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (REPORT_FINISHED_METHOD.equals(method)) {
            try {
                reportFinished(extraState);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("No such method '" + method + "'");
        }
    }

    private static void reportFailure(String message) throws IOException {
        System.out.println("Failure state: " + message);

        dumpPublicFile(FAILURE_FILE_PATH, message);
    }

    private static void reportFinished(StatusReporter.FinishedState finishedState) throws IOException {
        System.out.println("Finished state: " + finishedState.toString());

        dumpPublicFile(FINISHED_FILE_PATH, finishedState.toString());
    }

    private static void dumpPublicFile(String path, String content) throws IOException {
        File outputFile = getOutputFile(path);
        try (OutputStream fileOutputStream = new FileOutputStream(outputFile, false)) {
            fileOutputStream.write(content.getBytes());
            System.out.println("File: " + outputFile.getAbsolutePath() + " successfully saved");
        }

        if (!outputFile.setReadable(true, false)) {
            System.err.println("WARNING: Failed to make file " + outputFile.getAbsolutePath() + " readable!");
        }
    }

    private static File getOutputFile(String path) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), path);
    }
}
