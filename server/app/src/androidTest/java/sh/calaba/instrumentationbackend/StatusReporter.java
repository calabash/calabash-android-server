package sh.calaba.instrumentationbackend;

import android.support.annotation.StringDef;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StatusReporter {

    public static final String REPORT_FAILURE_METHOD = "report-failure";
    public static final String REPORT_FINISHED_METHOD = "report-finished";

    @StringDef({REPORT_FAILURE_METHOD, REPORT_FINISHED_METHOD})
    public @interface ReportMethod {}

    private boolean hasReportedFailure;

    public enum FinishedState {SUCCESSFUL, NOT_SUCCESSFUL}

    public StatusReporter() {
        this.hasReportedFailure = false;
    }

    public void reportFailure(String message) {
        StatusReporterObject.report(REPORT_FAILURE_METHOD, message, null);
        hasReportedFailure = true;
    }

    public void reportFailure(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("Unknown error:\n");
        e.printStackTrace(new PrintWriter(stringWriter));

        reportFailure(stringWriter.toString());
    }

    public void reportFinished(FinishedState state) {
        StatusReporterObject.report(REPORT_FINISHED_METHOD, null, state);
    }

    public boolean hasReportedFailure() {
        return hasReportedFailure;
    }
}
