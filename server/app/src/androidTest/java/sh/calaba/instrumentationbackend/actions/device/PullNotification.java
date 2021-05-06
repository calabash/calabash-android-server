package sh.calaba.instrumentationbackend.actions.device;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

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
