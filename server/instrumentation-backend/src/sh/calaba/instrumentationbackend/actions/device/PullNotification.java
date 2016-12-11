package sh.calaba.instrumentationbackend.actions.device;

import android.support.test.uiautomator.UiDevice;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * Created by rajdeepvarma on 10/12/16.
 */
public class PullNotification implements Action {
    @Override
    public Result execute(String... args) {
        InstrumentationBackend.getUiDevice().openNotification();
        return new Result(true);
    }

    @Override
    public String key() {
        return "pull_notification";
    }
}
