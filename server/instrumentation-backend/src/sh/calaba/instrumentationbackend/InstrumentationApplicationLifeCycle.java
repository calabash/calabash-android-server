package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

/**
 * Handles application life cycle using instrumentation
 */
public class InstrumentationApplicationLifeCycle implements ApplicationLifeCycle {
    private Instrumentation instrumentation;

    public InstrumentationApplicationLifeCycle(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
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

    public static Intent defaultStartIntent() {
        Intent defaultStartIntent = new Intent(Intent.ACTION_MAIN);
        defaultStartIntent.setClassName(InstrumentationBackend.testPackage,
                InstrumentationBackend.mainActivityName);
        defaultStartIntent.addCategory("android.intent.category.LAUNCHER");
        defaultStartIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        return defaultStartIntent;
    }
}
