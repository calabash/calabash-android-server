package sh.calaba.instrumentationbackend.actions.device;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollableCustom;
import androidx.test.uiautomator.UiSelector;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

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
                scrollable.scrollIntoView(clearButton, "scrollForward");
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
