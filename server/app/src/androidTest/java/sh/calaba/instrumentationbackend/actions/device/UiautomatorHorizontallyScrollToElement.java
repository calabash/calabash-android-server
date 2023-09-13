package sh.calaba.instrumentationbackend.actions.device;

import java.lang.reflect.InvocationTargetException;

import androidx.test.uiautomator.UiObjectNotFoundException;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

import static sh.calaba.instrumentationbackend.actions.device.ScrollToElementActionHelper.scrollToTargetInContainer;

public class UiautomatorHorizontallyScrollToElement implements Action {
    @Override
    public Result execute(String... args) {
        InstrumentationBackend.getUiDevice();
        try {
            String targetBySelectorStrategy = args[0];
            String targetLocator = args[1];

            String containerBySelectorStrategy = null;
            String containerLocator = null;
            if (args.length >= 4) {
                containerBySelectorStrategy = args[2];
                containerLocator = args[3];
            }
            int maxScrolls = 10;
            if (args.length >= 5) {
                maxScrolls = Integer.parseInt(args[4]);
            }

            scrollToTargetInContainer(targetBySelectorStrategy, targetLocator, containerBySelectorStrategy,
                    containerLocator, maxScrolls, true);
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
    public String key() { return "uiautomator_horizontally_scroll_to_element"; }
}
