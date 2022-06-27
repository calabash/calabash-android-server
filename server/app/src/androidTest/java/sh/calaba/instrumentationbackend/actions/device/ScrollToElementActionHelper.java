package sh.calaba.instrumentationbackend.actions.device;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollableCustom;
import androidx.test.uiautomator.UiSelector;

import static sh.calaba.instrumentationbackend.actions.device.StrategyUtils.convertBySelectorStrategyToUiSelectorStrategy;
import static sh.calaba.instrumentationbackend.actions.device.StrategyUtils.verifyStrategy;

public class ScrollToElementActionHelper {

    public static void scrollToTargetInContainer(String targetBySelectorStrategy, String targetLocator,
          String containerBySelectorStrategy, String containerLocator, int maxScrolls, boolean isHorizontal)
          throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, UiObjectNotFoundException {
        UiSelector targetViewSelector = getUiSelector(targetBySelectorStrategy, targetLocator);

        UiSelector scrollViewSelector = new UiSelector().scrollable(true);
        if (containerBySelectorStrategy != null && containerLocator != null) {
            scrollViewSelector = getUiSelector(containerBySelectorStrategy, containerLocator);
        }

        UiScrollableCustom scrollable = new UiScrollableCustom(scrollViewSelector);
        scrollable.setMaxSearchSwipes(maxScrolls);

        if (isHorizontal) {
            scrollable.setAsHorizontalList();
        } else {
            scrollable.setAsVerticalList();
        }

        if (!scrollable.scrollIntoView(targetViewSelector, true)) {
            String errorMessage = String.format("Found no elements for locator: %s by strategy: %s",
                  targetLocator, targetBySelectorStrategy);
            throw new UiObjectNotFoundException(errorMessage);
        }
    }

    private static UiSelector getUiSelector(String targetBySelectorStrategy, String targetLocator)
          throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        verifyStrategy(targetBySelectorStrategy);
        String targetUiSelectorStrategy = convertBySelectorStrategyToUiSelectorStrategy(targetBySelectorStrategy);

        Method targetStrategyMethod = UiSelector.class.getDeclaredMethod(targetUiSelectorStrategy, String.class);
        return ((UiSelector) targetStrategyMethod
              .invoke(new UiSelector(), targetLocator));
    }
}
