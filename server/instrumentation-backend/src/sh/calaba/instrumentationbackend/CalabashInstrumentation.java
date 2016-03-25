package sh.calaba.instrumentationbackend;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.os.IBinder;
import android.os.UserHandle;
import sh.calaba.exposed.InstrumentationExposed;
import sh.calaba.instrumentationbackend.actions.Actions;
import sh.calaba.instrumentationbackend.actions.HttpServer;
import sh.calaba.instrumentationbackend.automation.ApplicationUnderTest;
import sh.calaba.instrumentationbackend.automation.CalabashAutomationEmbedded;
import sh.calaba.instrumentationbackend.intenthook.IIntentHook;
import sh.calaba.instrumentationbackend.intenthook.IntentHookResult;
import sh.calaba.instrumentationbackend.utils.MonoUtils;

/*
    Entry point for Calabash based on Android instrumentation
 */
public class CalabashInstrumentation extends InstrumentationExposed {
    private String testPackage;
    private String mainActivityName;
    private Bundle extras;
    private Intent activityIntent;

    @Override
    public void onCreate(Bundle arguments) {
        StatusReporter statusReporter = new StatusReporter(getContext());

        try {
            final String mainActivity;

            if (arguments.containsKey("main_activity")
                    && arguments.getString("main_activity") != null
                    && !"null".equals(arguments.getString("main_activity"))) {
                mainActivity = arguments.getString("main_activity");
            } else {
                mainActivity = detectMainActivity(statusReporter, arguments.getString("target_package"));

                System.out.println("Main activity name automatically set to: " + mainActivity);

                if (mainActivity == null || "".equals(mainActivity)) {
                    statusReporter.reportFailure("E_COULD_NOT_DETECT_MAIN_ACTIVITY");
                    throw new RuntimeException("Could not detect main activity");
                }
            }

            MonoUtils.loadMono(getTargetContext());

            try {
                // Start the HttpServer as soon as possible in a not-ready state
                HttpServer.instantiate(Integer.parseInt(arguments.getString("test_server_port")));
            } catch (RuntimeException e) {
                if (getTargetContext().checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    statusReporter.reportFailure("E_NO_INTERNET_PERMISSION");
                }

                throw e;
            }

            Bundle extras = (Bundle) arguments.clone();
            extras.remove("target_package");
            extras.remove("main_activity");
            extras.remove("test_server_port");
            extras.remove("class");

            if (extras.isEmpty()) {
                extras = null;
            }

            this.testPackage = arguments.getString("target_package");
            this.mainActivityName = mainActivity;
            this.extras = extras;

            if (arguments.containsKey("intent_parcel")) {
                activityIntent = arguments.getParcelable("intent_parcel");
            }

            InstrumentationBackend.setDefaultCalabashAutomation(
                    new CalabashAutomationEmbedded(
                            new ApplicationUnderTestInstrumentation(addMonitor((IntentFilter) null, null, false))));

            InstrumentationBackend.instrumentation = this;
            InstrumentationBackend.actions = new Actions(this);

            super.onCreate(arguments);

            startTestServer();
        } catch (RuntimeException e) {
            if (!statusReporter.hasReportedFailure()) {
                statusReporter.reportFailure(e);
            }

            throw e;
        }
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

    private void startTestServer() {
        Intent defaultStartIntent;

        if (activityIntent != null) {
            defaultStartIntent = activityIntent;
        } else {
            defaultStartIntent = new Intent(Intent.ACTION_MAIN);
            defaultStartIntent.setClassName(this.testPackage,
                    this.mainActivityName);
            defaultStartIntent.addCategory("android.intent.category.LAUNCHER");
            defaultStartIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            defaultStartIntent.replaceExtras(this.extras);
        }

        final InstrumentationApplicationLifeCycle applicationLifeCycle =
                new InstrumentationApplicationLifeCycle(this, defaultStartIntent);

        final HttpTestServerLifeCycle testServerLifeCycle =
                new HttpTestServerLifeCycle(HttpServer.getInstance(), applicationLifeCycle);

        testServerLifeCycle.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (testServerLifeCycle.isHttpServerRunning()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("Thread is interrupted, breaking.");
                        break;
                    }
                }


                runOnMainSync(new Runnable() {
                    @Override
                    public void run() {
                        testServerLifeCycle.stop();
                        InstrumentationBackend.tearDown();
                    }
                });
            }
        }).start();
    }

    private String detectMainActivity(StatusReporter statusReporter, String targetPackage) {
        PackageManager packageManager = getTargetContext().getPackageManager();
        Intent launchIntent =
                packageManager.getLaunchIntentForPackage(targetPackage);

        if (launchIntent == null) {
            statusReporter.reportFailure("E_NO_LAUNCH_INTENT_FOR_PACKAGE");
            throw new RuntimeException("No launch intent set for package '" + targetPackage + "'");
        }

        String mainActivityTmpName = launchIntent.getComponent().getClassName();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(targetPackage,
                    PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activityInfoArr = packageInfo.activities;

            for (ActivityInfo activityInfo : activityInfoArr) {
                if (activityInfo.name.equals(mainActivityTmpName) &&
                        activityInfo.targetActivity != null) {
                    mainActivityTmpName = activityInfo.targetActivity;
                    break;
                }
            }

            return mainActivityTmpName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final class ApplicationUnderTestInstrumentation implements ApplicationUnderTest {
        private Instrumentation.ActivityMonitor activityMonitor;

        public ApplicationUnderTestInstrumentation(Instrumentation.ActivityMonitor activityMonitor) {
            this.activityMonitor = activityMonitor;
        }

        @Override
        public Application getApplication() {
            return activityMonitor.getLastActivity().getApplication();
        }

        @Override
        public Activity getCurrentActivity() {
            return activityMonitor.getLastActivity();
        }
    }
}
