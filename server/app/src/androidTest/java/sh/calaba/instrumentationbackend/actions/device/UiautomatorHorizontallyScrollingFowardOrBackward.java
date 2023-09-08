package sh.calaba.instrumentationbackend.actions.device;

import androidx.test.uiautomator.UiObjectNotFoundException;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

import java.lang.reflect.InvocationTargetException;

import static sh.calaba.instrumentationbackend.actions.device.ScrollToElementActionHelper.scrollToTargetByDirection;

/**
 * The class can be used to horizontally scroll to the target element, either scrolling horizontally forward or backward.
 * The command to run horizontally forward example: perform_action('uiautomator_horizontally_scroll_forward_or_backward_to_element', 'FORWARD', 'text', 'Android', '10')
 * The command to run horizontally backward example: perform_action('uiautomator_horizontally_scroll_forward_or_backward_to_element', 'BACKWARD', 'text', 'Android', '10')
 * Documentation for the above can be found at https://github.com/calabash/calabash-android/wiki/UIAutomator2
 */
public class UiautomatorHorizontallyScrollingFowardOrBackward implements Action {
    @Override
    public Result execute(String... args) {
        InstrumentationBackend.getUiDevice();
        try {
            String direction = args[0];
            String targetBySelectorStrategy = args[1];
            String targetLocator = args[2];

            int maxScrolls = 10;
            if (args.length >= 3) {
                maxScrolls = Integer.parseInt(args[3]);
            }

            ScrollDirection scrollDirection = ScrollDirection.valueOf(direction);

            scrollToTargetByDirection(targetBySelectorStrategy, targetLocator, scrollDirection, maxScrolls, true);
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
    public String key() { return "uiautomator_horizontally_scroll_forward_or_backward_to_element"; }
}
