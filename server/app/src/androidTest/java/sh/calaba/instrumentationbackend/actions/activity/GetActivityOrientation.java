package sh.calaba.instrumentationbackend.actions.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class GetActivityOrientation implements Action {

    @Override
    public Result execute(String... args) {
        Activity activity = InstrumentationBackend.getCurrentActivity();
        final int orientation = activity.getRequestedOrientation();

        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return new Result(true, "landscape");
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                return new Result(true, "reverse_landscape");
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return new Result(true, "portrait");
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                return new Result(true, "reverse_portrait");
            default:
                return Result.failedResult("Invalid orientation '" + orientation + "' for activity '" + activity + "'");
        }
    }

    @Override
    public String key() {
        return "get_activity_orientation";
    }
}
