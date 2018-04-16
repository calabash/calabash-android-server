package sh.calaba.instrumentationbackend.actions.device;

import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * Created by rajdeepvarma on 10/12/16.
 */
public class UiautomatorTouchPartialText implements Action {
    @Override
    public Result execute(String... args) {
        UiObject element = InstrumentationBackend.getUiDevice().findObject(new UiSelector().textContains(args[0]));
        try {
            element.click();
        } catch (UiObjectNotFoundException e) {
            String message = e.getMessage();
            return Result.failedResult(message);
        }
        return new Result(true);
    }

    @Override
    public String key() {
        return "uiautomator_touch_partial_text";
    }
}
