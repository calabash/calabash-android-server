package sh.calaba.instrumentationbackend.actions.device;

import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollableCustom;
import android.support.test.uiautomator.UiSelector;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * Created by rajdeepvarma on 14/12/16.
 */
public class ClearNotificationPanel implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice device = InstrumentationBackend.getUiDevice();
        UiSelector clearButton = new UiSelector().descriptionContains("clear");
        try {
            if (device.findObject(clearButton).exists()) {
                device.findObject(clearButton).click();
                return new Result(true);
            } else {
                UiSelector notificationStackScroller = new UiSelector().resourceId("com.android.systemui:id/notification_stack_scroller");
                UiScrollableCustom scrollable = new UiScrollableCustom(notificationStackScroller);
                scrollable.scrollIntoView(clearButton);
                device.findObject(clearButton).click();
            }

        } catch (UiObjectNotFoundException e) {
            String message = e.getMessage();
            return Result.failedResult(message);
        }
        return new Result(true);
    }

    @Override
    public String key() {
        return "clear_notifications";
    }
}
