package sh.calaba.instrumentationbackend.entrypoint;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import sh.calaba.instrumentationbackend.*;
import sh.calaba.instrumentationbackend.actions.Actions;
import sh.calaba.instrumentationbackend.actions.HttpServer;
import sh.calaba.instrumentationbackend.automation.CalabashAutomation;
import sh.calaba.instrumentationbackend.automation.CalabashAutomationEmbedded;
import sh.calaba.instrumentationbackend.utils.MonoUtils;

/**
 * Entry point for calabash started using am instrument / hitting ActivityManagerService with instrumentation
 */
public class AndroidInstrumentationStartup implements EntryPoint {
    private Intent activityIntent;
    private String mainActivityName;
    private Bundle extras;

    private final CalabashInstrumentation instrumentation;
    private final Bundle arguments;

    public static class Factory {
        public static EntryPoint newInstance(CalabashInstrumentation instrumentation, Bundle arguments) {
            return new AndroidInstrumentationStartup(instrumentation, arguments);
        }
    }

    private AndroidInstrumentationStartup(CalabashInstrumentation instrumentation, Bundle arguments) {
        InstrumentationRegistry.registerInstance(instrumentation, arguments);
        this.instrumentation = instrumentation;
        this.arguments = arguments;
    }

    @Override
    public void start() {
        StatusReporter statusReporter = new StatusReporter();

        try {
            final String mainActivity;

            if (arguments.containsKey("main_activity")
                    && arguments.getString("main_activity") != null
                    && !"null".equals(arguments.getString("main_activity"))) {
                mainActivity = arguments.getString("main_activity");
            } else {
                mainActivity = detectMainActivity(statusReporter, instrumentation.getTargetContext().getPackageName());

                System.out.println("Main activity name automatically set to: " + mainActivity);

                if (mainActivity == null || "".equals(mainActivity)) {
                    statusReporter.reportFailure(instrumentation, "E_COULD_NOT_DETECT_MAIN_ACTIVITY");
                    throw new RuntimeException("Could not detect main activity");
                }
            }

            MonoUtils.loadMono(instrumentation.getTargetContext());

            Logger.info("Test server port: " + arguments.getString("test_server_port"));

            Bundle extras = (Bundle) arguments.clone();
            extras.remove("main_activity");
            extras.remove("test_server_port");
            extras.remove("class");

            if (extras.isEmpty()) {
                extras = null;
            }

            this.mainActivityName = mainActivity;
            this.extras = extras;

            if (arguments.containsKey("intent_parcel")) {
                activityIntent = arguments.getParcelable("intent_parcel");
            }

            if ("NO_START".equals(mainActivity)) {
                if (instrumentation.getTargetContext() instanceof Activity) {
                    ((Activity) instrumentation.getTargetContext()).finish();
                }

                if (instrumentation.getContext() instanceof Activity) {
                    ((Activity) instrumentation.getContext()).finish();
                }

                return;
            }

            setup();

            try {
                startTestServer();
            } catch (RuntimeException e) {
                if (instrumentation.getContext().checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    statusReporter.reportFailure(instrumentation, "E_NO_INTERNET_PERMISSION");
                }

                throw e;
            }
        } catch (RuntimeException e) {
            if (!statusReporter.hasReportedFailure()) {
                statusReporter.reportFailure(instrumentation, e);
            }

            throw e;
        }
    }

    @Override
    public CalabashAutomation getCalabashAutomation() {
        return new CalabashAutomationEmbedded(
                new ApplicationUnderTestInstrumentation(this.instrumentation));
    }

    private void setup() {
        // TODO: Remove this over time
        InstrumentationBackend.instrumentation = instrumentation;
        InstrumentationBackend.actions = new Actions(instrumentation);
    }

    private void startTestServer() {
        Intent defaultStartIntent;

        if (activityIntent != null) {
            defaultStartIntent = activityIntent;
        } else {
            defaultStartIntent = new Intent(Intent.ACTION_MAIN);
            defaultStartIntent.setClassName(instrumentation.getTargetContext().getPackageName(),
                    this.mainActivityName);
            defaultStartIntent.addCategory("android.intent.category.LAUNCHER");
            defaultStartIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            defaultStartIntent.replaceExtras(this.extras);
        }

        HttpServer.instantiateAndListen(Integer.parseInt(arguments.getString("test_server_port")));

        final CalabashInstrumentationApplicationLifeCycle applicationLifeCycle =
                new CalabashInstrumentationApplicationLifeCycle(instrumentation, defaultStartIntent);

        final HttpTestServerLifeCycle testServerLifeCycle =
                new HttpTestServerLifeCycle(HttpServer.getInstance(), applicationLifeCycle);

        testServerLifeCycle.startAndWaitForKill();
    }

    private String detectMainActivity(StatusReporter statusReporter, String targetPackage) {
        PackageManager packageManager = instrumentation.getTargetContext().getPackageManager();
        Intent launchIntent =
                packageManager.getLaunchIntentForPackage(targetPackage);

        if (launchIntent == null) {
            statusReporter.reportFailure(instrumentation, "E_NO_LAUNCH_INTENT_FOR_PACKAGE");
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
}
