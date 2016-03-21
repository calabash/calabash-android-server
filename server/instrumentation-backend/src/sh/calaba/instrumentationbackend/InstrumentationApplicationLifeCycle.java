package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

/**
 * Handles application life cycle using instrumentation
 */
public class InstrumentationApplicationLifeCycle implements ApplicationLifeCycle {
    private Instrumentation instrumentation;
    private Intent defaultIntent;

    public InstrumentationApplicationLifeCycle(Instrumentation instrumentation, Intent defaultIntent) {
        this.instrumentation = instrumentation;
        this.defaultIntent = defaultIntent;
    }

    @Override
    public Activity start(Intent startIntent) {
        Intent startIntentAdded;

        if (startIntent != null) {
            startIntentAdded = new Intent(startIntent);
        } else {
            startIntentAdded = new Intent(defaultStartIntent());
        }

        startIntentAdded.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instrumentation.setInTouchMode(true);

        return getInstrumentation().startActivitySync(startIntentAdded);
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public Intent defaultStartIntent() {
        return defaultIntent;
    }
}
