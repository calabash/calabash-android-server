package sh.calaba.instrumentationbackend;

import android.app.Instrumentation;
import android.os.Bundle;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StatusReporter {

    public static final String REPORT_MESSAGE = "report-message";
    public static final int RESULT_CODE_OK = 0;
    public static final int RESULT_CODE_FAILED = 1;

    private boolean hasReportedFailure;

    public enum FinishedState {SUCCESSFUL, NOT_SUCCESSFUL}

    public StatusReporter() {
        this.hasReportedFailure = false;
    }

    public void reportFailure(Instrumentation instr, String message) {
        hasReportedFailure = true;

        Bundle results = createReport(message);
        instr.finish(RESULT_CODE_FAILED, results);
    }

    public void reportFailure(Instrumentation instr, Throwable e) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("Unknown error:\n");
        e.printStackTrace(new PrintWriter(stringWriter));

        reportFailure(instr, stringWriter.toString());
    }

    public void reportFinished(Instrumentation instr, FinishedState state) {
        Bundle results = createReport(state.name());
        instr.finish(RESULT_CODE_OK, results);
    }

    public boolean hasReportedFailure() {
        return hasReportedFailure;
    }

    private Bundle createReport(String message) {
        Bundle results = new Bundle();
        results.putString(REPORT_MESSAGE, message);
        return results;
    }
}
