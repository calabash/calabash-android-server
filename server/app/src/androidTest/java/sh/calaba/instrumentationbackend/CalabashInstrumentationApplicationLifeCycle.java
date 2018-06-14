package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import sh.calaba.instrumentationbackend.utils.ActivityLaunchedWaiter;

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

        // solves the issue of Android waiting for the message queue of the application
        // to become idle before continuing from {@link android.app.Instrumentation#startActivitySync}.
        // See ActivityLaunchedWaiter class for details
        Thread t = new Thread(new ActivityLaunchedWaiter(getInstrumentation()), "ActivityLaunchedWaiter-Thread");
        t.start();
        Activity activity = getInstrumentation().startActivitySync(startIntentAdded);
        t.interrupt();

        return activity;
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
