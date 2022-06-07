package sh.calaba.instrumentationbackend.actions.device;

import java.util.List;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class UiautomatorScrollTest implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();

        try {
            int index = Integer.parseInt(args[0]);
            int percentage = Integer.parseInt(args[1]);
            int speed = Integer.parseInt(args[2]);

            BySelector scrollableSelector = By.scrollable(true);
            List<UiObject2> matchingObjects = mDevice.findObjects(scrollableSelector);
            if (matchingObjects.isEmpty()) { //TODO add index check
                String errorMessage = String.format("Scrollable views not found.");
                throw new UiObjectNotFoundException(errorMessage);
            }
            if (matchingObjects.size() <= index) {
                throw new UiObjectNotFoundException(String.format("No scrollable found at index %d", index));
            }
            UiObject2 scrollableObject2 = matchingObjects.get(index);
            boolean scrolled = scrollableObject2.scroll(Direction.DOWN, percentage, speed);
            if (!scrolled) {
                throw new IllegalStateException("It was not possible to scroll");
            }
        } catch (UiObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
        return new Result(true);
    }

    @Override
    public String key() { return "uiautomator_scroll_test"; }
}

