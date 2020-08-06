package sh.calaba.instrumentationbackend;

import android.content.Context;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StatusReporter {
    private Context context;
    private boolean hasReportedFailure;

    public StatusReporter(Context context) {
        this.context = context;
        this.hasReportedFailure = false;
    }

    public void reportFailure(String message) {
        StatusReporterObject.report(context, "report-failure", message, null, true);
        hasReportedFailure = true;
    }

    public void reportFailure(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("Unknown error:\n");
        e.printStackTrace(new PrintWriter(stringWriter));

        reportFailure(stringWriter.toString());
    }

    public void clear() {
        StatusReporterObject.report(context, "clear", null, null, true);
    }

    public enum FinishedState {SUCCESSFUL, NOT_SUCCESSFUL}

    ;

    public void reportFinished(FinishedState state) {
        StatusReporterObject.report(context, "report-finished", null, state, true);
    }

    public boolean hasReportedFailure() {
        return hasReportedFailure;
    }
}
