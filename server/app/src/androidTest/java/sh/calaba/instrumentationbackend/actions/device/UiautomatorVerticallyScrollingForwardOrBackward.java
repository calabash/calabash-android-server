package sh.calaba.instrumentationbackend.actions.device;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * The class can be used to vertically scroll to the target element, either scrolling vertically forward or backward.
 * The command to run vertically forward example: perform_action('uiautomator_vertically_scroll_forward_or_backward_to_element', 'FORWARD', 'text', 'Android', '10')
 * The command to run vertically backward example: perform_action('uiautomator_vertically_scroll_forward_or_backward_to_element', 'BACKWARD', 'text', 'Android', '10')
 * Documentation for the above can be found at https://github.com/calabash/calabash-android/wiki/UIAutomator2
 */

public class UiautomatorVerticallyScrollingForwardOrBackward implements Action {

    private final UiautomatorScrollingForwardOrBackward scrollingAction;
    public UiautomatorVerticallyScrollingForwardOrBackward() {
        this.scrollingAction = new UiautomatorScrollingForwardOrBackward(
                "uiautomator_vertically_scroll_forward_or_backward_to_element",
                false
        );
    }

    @Override
    public Result execute(String... args) {
        return scrollingAction.execute(args);
    }

    @Override
    public String key() {
        return scrollingAction.key();
    }

}
