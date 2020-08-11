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
        try {
            Intent intent = new Intent(context, StatusReporterActivity.class);
            intent.putExtra(StatusReporterActivity.EXTRA_METHOD, "report-failure");
            intent.putExtra(StatusReporterActivity.EXTRA_MESSAGE, message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable e) {
            StatusReporterObject.report(context, "report-failure", message, null, true);
        } finally {
            hasReportedFailure = true;
        }
    }

    public void reportFailure(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("Unknown error:\n");
        e.printStackTrace(new PrintWriter(stringWriter));

        reportFailure(stringWriter.toString());
    }

    public void clear() {
        try {
            Intent intent = new Intent(context, StatusReporterActivity.class);
            intent.putExtra(StatusReporterActivity.EXTRA_METHOD, "clear");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable e) {
            StatusReporterObject.report(context, "clear", null, null, true);
        }
    }

    public enum FinishedState {SUCCESSFUL, NOT_SUCCESSFUL}

    ;

    public void reportFinished(FinishedState state) {
        try {
            Intent intent = new Intent(context, StatusReporterActivity.class);
            intent.putExtra(StatusReporterActivity.EXTRA_METHOD, "report-finished");
            intent.putExtra(StatusReporterActivity.EXTRA_STATE, state);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Throwable e) {
            StatusReporterObject.report(context, "report-finished", null, state, true);
        }
    }

    public boolean hasReportedFailure() {
        return hasReportedFailure;
    }
}
