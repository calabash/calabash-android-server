package sh.calaba.instrumentationbackend;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import android.os.IBinder;
import android.os.UserHandle;
import sh.calaba.exposed.InstrumentationExposed;
import sh.calaba.instrumentationbackend.entrypoint.AndroidInstrumentationStartup;
import sh.calaba.instrumentationbackend.intenthook.IIntentHook;
import sh.calaba.instrumentationbackend.intenthook.IntentHookResult;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

public class CalabashInstrumentation extends InstrumentationExposed {
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

    @Override
    public void execStartActivities(Context who, IBinder contextThread, IBinder token, Activity target, Intent[] intents, Bundle options) {
        Logger.info("execStartActivity 1");
        // We have no hooks for this
        super.execStartActivities(who, contextThread, token, target, intents, options);
    }

    @Override
    public void execStartActivitiesAsUser(Context who, IBinder contextThread, IBinder token, Activity target, Intent[] intents, Bundle options, int userId) {
        Logger.info("execStartActivity 2");
        // We have no hooks for this
        super.execStartActivitiesAsUser(who, contextThread, token, target, intents, options, userId);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public ActivityResult execStartActivity(final Context who, final IBinder contextThread, final IBinder token, final Fragment target, final Intent intent, final int requestCode) {
        Logger.info("execStartActivity 8");
        Activity activity;

        if (target == null) {
            activity = null;
        } else {
            activity = target.getActivity();
        }

        return handleExecStartActivity(intent, activity, new ExecStartActivityHandler() {
            @Override
            public IntentHookResult whenFiltered(IIntentHook intentHook) {
                return intentHook.execStartActivity(who, contextThread, token, target, intent, requestCode, null);
            }

            @Override
            public ActivityResult whenUnhandled(Intent modifiedIntent) {
                return CalabashInstrumentation.super.execStartActivity(who, contextThread, token, target, modifiedIntent, requestCode);
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public ActivityResult execStartActivity(final Context who, final IBinder contextThread, final IBinder token, final Fragment target, final Intent intent, final int requestCode, final Bundle options) {
        Logger.info("execStartActivity 3");
        Activity activity;

        if (target == null) {
            activity = null;
        } else {
            activity = target.getActivity();
        }

        return handleExecStartActivity(intent, activity, new ExecStartActivityHandler() {
            @Override
            public IntentHookResult whenFiltered(IIntentHook intentHook) {
                return intentHook.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
            }

            @Override
            public ActivityResult whenUnhandled(Intent modifiedIntent) {
                return CalabashInstrumentation.super.execStartActivity(who, contextThread, token, target, modifiedIntent, requestCode, options);
            }
        });
    }

    // Instead of creating a new method for this, we simply pass the bundle as null.
    @Override
    public ActivityResult execStartActivity(final Context who, final IBinder contextThread, final IBinder token, final Activity target, final Intent intent, final int requestCode) {
        Logger.info("execStartActivity 7");

        return handleExecStartActivity(intent, target, new ExecStartActivityHandler() {
            @Override
            public IntentHookResult whenFiltered(IIntentHook intentHook) {
                return intentHook.execStartActivity(who, contextThread,
                        token, target, intent, requestCode, null);
            }

            @Override
            public ActivityResult whenUnhandled(Intent modifiedIntent) {
                return CalabashInstrumentation.super.execStartActivity(who, contextThread, token, target, modifiedIntent, requestCode);
            }
        });
    }

    @Override
    public ActivityResult execStartActivity(final Context who, final IBinder contextThread, final IBinder token, final Activity target, final Intent intent, final int requestCode, final Bundle options) {
        Logger.info("execStartActivity 4");

        return handleExecStartActivity(intent, target, new ExecStartActivityHandler() {
            @Override
            public IntentHookResult whenFiltered(IIntentHook intentHook) {
                return intentHook.execStartActivity(who, contextThread,
                        token, target, intent, requestCode, options);
            }

            @Override
            public ActivityResult whenUnhandled(Intent modifiedIntent) {
                return CalabashInstrumentation.super.execStartActivity(who, contextThread, token, target, modifiedIntent, requestCode, options);
            }
        });
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options, UserHandle user) {
        Logger.info("execStartActivity 5");
        // We have no hooks for this
        return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options, user);
    }

    @Override
    public ActivityResult execStartActivityAsCaller(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options, int userId) {
        Logger.info("execStartActivity 6");
        // We have no hooks for this
        return super.execStartActivityAsCaller(who, contextThread, token, target, intent, requestCode, options, userId);
    }

    public ActivityResult handleExecStartActivity(final Intent intent, final Activity target, ExecStartActivityHandler handler) {
        InstrumentationBackend.intents.add(intent);

        if (InstrumentationBackend.shouldFilter(intent, target)) {
            IIntentHook intentHook = InstrumentationBackend.useIntentHookFor(intent, target);
            IntentHookResult intentHookResult = handler.whenFiltered(intentHook);

            if (intentHookResult.isHandled()) {
                return intentHookResult.getActivityResult();
            } else {
                final Intent modifiedIntent;

                if (intentHookResult.getModifiedIntent() != null) {
                    modifiedIntent = intentHookResult.getModifiedIntent();
                } else {
                    modifiedIntent = intent;
                }

                return handler.whenUnhandled(modifiedIntent);
            }
        }

        return handler.whenUnhandled(intent);
    }


    private interface ExecStartActivityHandler {
        public IntentHookResult whenFiltered(IIntentHook intentHook);

        public ActivityResult whenUnhandled(Intent modifiedIntent);
    }
}
