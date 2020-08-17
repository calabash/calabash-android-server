package sh.calaba.instrumentationbackend;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StatusReporter {

    private boolean hasReportedFailure;

    public enum FinishedState {SUCCESSFUL, NOT_SUCCESSFUL}

    public StatusReporter() {
        this.hasReportedFailure = false;
    }

    public void reportFailure(String message) {
        StatusReporterObject.report("report-failure", message, null, true);
        hasReportedFailure = true;
    }

    public void reportFailure(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("Unknown error:\n");
        e.printStackTrace(new PrintWriter(stringWriter));

        reportFailure(stringWriter.toString());
    }

    public void reportFinished(FinishedState state) {
        StatusReporterObject.report("report-finished", null, state, true);
    }

    public boolean hasReportedFailure() {
        return hasReportedFailure;
    }
}
