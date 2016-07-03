package sh.calaba.instrumentationbackend.actions.activity;

import android.content.pm.ActivityInfo;
import android.os.Build;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class SetActivityOrientation implements Action {

    @Override
    public Result execute(String... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No orientation provided. Use 'landscape' or 'portrait'");
        }

        String orientation = args[0].toLowerCase();

        final int requestedScreenOrientation;

        if (orientation.equals("landscape")) {
            requestedScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (orientation.equals("reverse_landscape")) {
            if (Build.VERSION.SDK_INT > 8) {
                requestedScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                requestedScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else if (orientation.equals("portrait")) {
            requestedScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (orientation.equals("reverse_portrait")) {
            if (Build.VERSION.SDK_INT > 8) {
                requestedScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            } else {
                requestedScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else {
            throw new IllegalArgumentException("Invalid orientation '" + orientation + "'. Use 'landscape' or 'portrait'");
        }

        InstrumentationBackend.getCurrentActivity().setRequestedOrientation(requestedScreenOrientation);

        // Wait 100ms for orientation change to happen.
        sleep(100);

        return Result.successResult();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String key() {
        return "set_activity_orientation";
    }
}
