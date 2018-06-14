package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.os.IBinder;
import sh.calaba.instrumentationbackend.entrypoint.AndroidInstrumentationStartup;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

public class CalabashInstrumentation extends Instrumentation {
    private Stack<WeakReference<Activity>> lastActivities = new Stack<WeakReference<Activity>>();

    // Android invokes onCreate automatically. dontRun is set by Main to ensure
    // that this entry point is not mistakenly started.
    public static boolean dontRun = false;

    // This method is called automatically when started using instrumentation
    // from activityservice (am instrument ...)
    @Override
    public void onCreate(Bundle arguments) {
        if (dontRun) {
            return;
        }

        // Entry-point for instrumentation with services
        Main.start(AndroidInstrumentationStartup.Factory.newInstance(this, arguments));
    }

    public WeakReference<Activity> getLastActivity() {
        return lastActivities.peek();
    }

    public Iterator<WeakReference<Activity>> getLastActivitiesIterator() {
        return lastActivities.iterator();
    }

    /*
        Hook for a new activity being created using instrumentation. Often called from ActivityThread
     */
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        Logger.info("newActivity1");
        Activity activity = super.newActivity(cl, className, intent);
        lastActivities.push(new WeakReference<Activity>(activity));

        return activity;
    }

    /*
        Hook for a new activity being created using instrumentation. Often called from ActivityThread
     */
    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent,
                                ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance)
            throws InstantiationException, IllegalAccessException {
        Logger.info("newActivity2");
        Activity activity = super.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
        lastActivities.push(new WeakReference<Activity>(activity));

        return activity;
    }

    /*
        Hook for an activity being resumed. This can either be a new activity, or because a user
        has pressed the back button etc.
     */
    @Override
    public void callActivityOnResume(Activity activity) {
        lastActivities.push(new WeakReference<Activity>(activity));

        super.callActivityOnResume(activity);
    }
}
