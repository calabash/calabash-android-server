package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import java.lang.ref.WeakReference;
import java.util.Iterator;

/**
 * Handles application life cycle using instrumentation
 */
public class CalabashInstrumentationApplicationLifeCycle implements ApplicationLifeCycle {
    private CalabashInstrumentation instrumentation;
    private Intent defaultIntent;

    public CalabashInstrumentationApplicationLifeCycle(CalabashInstrumentation instrumentation, Intent defaultIntent) {
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

    @Override
    public void stop() {
        Iterator<WeakReference<Activity>> iterator = instrumentation.getLastActivitiesIterator();

        while (iterator.hasNext()) {
            WeakReference<Activity> weakReference = iterator.next();

            if (weakReference != null) {
                Activity activity = weakReference.get();

                if (activity != null) {
                    activity.finish();
                }
            }
        }
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public Intent defaultStartIntent() {
        return defaultIntent;
    }
}
