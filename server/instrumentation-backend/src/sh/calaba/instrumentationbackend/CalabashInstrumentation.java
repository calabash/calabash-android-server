package sh.calaba.instrumentationbackend;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.jayway.android.robotium.solo.SoloEnhanced;

import sh.calaba.instrumentationbackend.actions.Actions;
import sh.calaba.instrumentationbackend.actions.HttpServer;
import sh.calaba.instrumentationbackend.utils.MonoUtils;

public class CalabashInstrumentation extends Instrumentation {
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

            InstrumentationBackend.testPackage = arguments.getString("target_package");

            Bundle extras = (Bundle) arguments.clone();
            extras.remove("target_package");
            extras.remove("main_activity");
            extras.remove("test_server_port");
            extras.remove("class");

            if (extras.isEmpty()) {
                extras = null;
            }

            InstrumentationBackend.extras = extras;
            InstrumentationBackend.mainActivityName = mainActivity;
            InstrumentationBackend.instrumentation = this;
            InstrumentationBackend.actions = new Actions(this);
            InstrumentationBackend.activityMonitor = addMonitor((IntentFilter) null, null, false);

            super.onCreate(arguments);

            startTestServer();
        } catch (RuntimeException e) {
            if (!statusReporter.hasReportedFailure()) {
                statusReporter.reportFailure(e);
            }

            throw e;
        }
    }

    private void startTestServer() {
        final InstrumentationApplicationLifeCycle applicationLifeCycle =
                new InstrumentationApplicationLifeCycle(this);

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
}
