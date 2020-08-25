package sh.calaba.instrumentationbackend;

import android.app.Instrumentation;
import android.os.Bundle;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StatusReporter {

    public static final String REPORT_MESSAGE = "report-message";
    public static final int RESULT_CODE_OK = 0;
    public static final int RESULT_CODE_FAILED = 1;

    private final Instrumentation instrumentation;
    private boolean hasReportedFailure;

    public enum FinishedState {SUCCESSFUL, NOT_SUCCESSFUL}

    public StatusReporter(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
        this.hasReportedFailure = false;
    }

    public void reportFailure(String message) {
        report(RESULT_CODE_FAILED, message);
        hasReportedFailure = true;
    }

    public void reportFailure(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("Unknown error:\n");
        e.printStackTrace(new PrintWriter(stringWriter));

        reportFailure(stringWriter.toString());
    }

    public void reportFinished(FinishedState state) {
        report(RESULT_CODE_OK, state.name());
    }

    public boolean hasReportedFailure() {
        return hasReportedFailure;
    }

    private void report(int resultCode, String reportMessage) {
        Bundle results = new Bundle();
        results.putString(REPORT_MESSAGE, reportMessage);

        instrumentation.sendStatus(resultCode, results);
    }
}
