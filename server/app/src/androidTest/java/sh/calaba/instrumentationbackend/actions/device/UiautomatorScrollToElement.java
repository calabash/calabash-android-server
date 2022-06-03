package sh.calaba.instrumentationbackend.actions.device;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollableCustom;
import androidx.test.uiautomator.UiSelector;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

import static sh.calaba.instrumentationbackend.actions.device.StrategyUtils.convertBySelectorStrategyToUiSelectorStrategy;
import static sh.calaba.instrumentationbackend.actions.device.StrategyUtils.verifyStrategy;

public class UiautomatorScrollToElement implements Action {
    @Override
    public Result execute(String... args) {
        InstrumentationBackend.getUiDevice();

        try {
            String bySelectorStrategy = args[0];
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

            verifyStrategy(bySelectorStrategy);
            String uiSelectorStrategy = convertBySelectorStrategyToUiSelectorStrategy(bySelectorStrategy);

            Method strategyMethod = UiSelector.class.getDeclaredMethod(uiSelectorStrategy, String.class);
            UiSelector targetViewSelector = ((UiSelector) strategyMethod.invoke(new UiSelector(), locator))
                  .instance(index);

            UiSelector scrollViewSelector = new UiSelector()
                  .scrollable(true)
                  .instance(scrollableIndex);
            UiScrollableCustom scrollable = new UiScrollableCustom(scrollViewSelector);
            scrollable.setMaxSearchSwipes(maxScrolls);

            if (!scrollable.scrollIntoView(targetViewSelector, true)) {
                String errorMessage = String.format("Found no elements for locator: %s by strategy: %s and index %d",
                      locator, bySelectorStrategy, index);
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

