package sh.calaba.instrumentationbackend;

import android.content.Context;
import android.content.Intent;

import sh.calaba.instrumentationbackend.utils.StringUtils;

/**
 * Deprecated to use for Android 11 and higher. Will cause a crash
 * Use {@link InstrumentationReport} instead
 */
@Deprecated
public class StatusReporter {
    private Context context;
    private boolean hasReportedFailure;

    public StatusReporter(Context context) {
        this.context = context;
        this.hasReportedFailure = false;
    }

    public void reportFailure(String message)  {
        Intent intent = new Intent(context, StatusReporterActivity.class);
        intent.putExtra(StatusReporterActivity.EXTRA_METHOD, "report-failure");
        intent.putExtra(StatusReporterActivity.EXTRA_MESSAGE, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        hasReportedFailure = true;
    }

    public void reportFailure(Throwable e) {
        reportFailure(StringUtils.toString(e));
    }

    public void clear() {
        Intent intent = new Intent(context, StatusReporterActivity.class);
        intent.putExtra(StatusReporterActivity.EXTRA_METHOD, "clear");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public enum FinishedState {SUCCESSFUL, NOT_SUCCESSFUL};

    public void reportFinished(FinishedState state)  {
        Intent intent = new Intent(context, StatusReporterActivity.class);
        intent.putExtra(StatusReporterActivity.EXTRA_METHOD, "report-finished");
        intent.putExtra(StatusReporterActivity.EXTRA_STATE, state);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean hasReportedFailure() {
        return hasReportedFailure;
    }
}
