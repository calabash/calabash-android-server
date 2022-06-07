package sh.calaba.instrumentationbackend.actions.device;

import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import static sh.calaba.instrumentationbackend.actions.device.StrategyUtils.verifyStrategy;

public class Uiautomator2ScrollToElement implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();

        try {
            String strategy = args[0];
            String locator = args[1];
            int index = Integer.parseInt(args[2]);
            int scrollableIndex = 0;
            if (args.length >= 4) {
                scrollableIndex = Integer.parseInt(args[3]);
            }
            int maxScrolls = 10;
            if (args.length >= 5) {
                maxScrolls = Integer.parseInt(args[4]);
            }

            verifyStrategy(strategy);

            BySelector scrollableSelector = By.scrollable(true);
            List<UiObject2> matchingObjects = mDevice.findObjects(scrollableSelector);
            if (matchingObjects.isEmpty() ) { //TODO add index check.
                String errorMessage = String.format("Scrollable views not found.");
                throw new UiObjectNotFoundException(errorMessage);
            }
            UiObject2 scrollableObject2 = matchingObjects.get(scrollableIndex);
            ScrollableUiObject2 scrollable2 = new ScrollableUiObject2(mDevice, scrollableObject2, Direction.DOWN);

            Method strategyMethod = By.class.getMethod(strategy, String.class);
            BySelector targetSelector = (BySelector) strategyMethod.invoke(By.class, locator);

            if (scrollable2.scrollIntoView(targetSelector, index, maxScrolls) == null) {
                String errorMessage = String.format("Found no elements for locator: %s by strategy: %s and index %d",
                      locator, strategy, index);
                throw new UiObjectNotFoundException(errorMessage);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (UiObjectNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return new Result(true);
    }

    @Override
    public String key() { return "uiautomator_scroll_to_element"; }
}

class ScrollableUiObject2 {
    private final UiDevice device;
    private final UiObject2 scrollableObject;
    private final Direction scrollDirection;

    public ScrollableUiObject2(UiDevice device, UiObject2 scrollableObject, Direction scrollDirection) {
        this.device = device;
        if (!scrollableObject.isScrollable()) {
            throw new IllegalArgumentException("The scrollableObject provided is not scrollable.");
        }
        this.scrollableObject = scrollableObject;
        this.scrollDirection = scrollDirection;
    }

    @Nullable
    public UiObject2 scrollIntoView(BySelector targetSelector, int targetIndex, int maxScrolls) {
        UiObject2 objectFound = null;
        for (int x = 0; x < maxScrolls && objectFound == null; ++x) {
            objectFound = getTargetIfVisible(targetSelector, targetIndex);
            if (objectFound == null) {
                boolean scrolled = scrollableObject.scroll(scrollDirection, 30);
                objectFound = getTargetIfVisible(targetSelector, targetIndex);
                if (!scrolled) break;
            }
        }
        return objectFound;
    }

    @Nullable
    private UiObject2 getTargetIfVisible(BySelector targetSelector, int targetIndex) {
        List<UiObject2> matchingObjects = device.findObjects(targetSelector);
        if (matchingObjects.size() >= targetIndex + 1) {
            return matchingObjects.get(targetIndex);
        }
        return null;
    }
}

