package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.os.Bundle;
import android.test.InstrumentationTestRunner;

import sh.calaba.instrumentationbackend.utils.MonoUtils;

/**
 * Deprecated to use for Android 11 and higher. Will cause a crash
 */
@Deprecated
public class ClearAppData2 extends InstrumentationTestRunner {
    @Override
    public void onCreate(Bundle arguments) {
        MonoUtils.loadMono(getTargetContext());

        StatusReporter statusReporter = new StatusReporter(getContext());

        try {
            Cleaner cleaner = new Cleaner();
            cleaner.clearAppData(getTargetContext());
        } catch (Exception e) {
            statusReporter.reportFailure(e);
            throw new RuntimeException(e);
        }

        statusReporter.reportFinished(StatusReporter.FinishedState.SUCCESSFUL);
        finish(Activity.RESULT_OK, null);
    }
}
