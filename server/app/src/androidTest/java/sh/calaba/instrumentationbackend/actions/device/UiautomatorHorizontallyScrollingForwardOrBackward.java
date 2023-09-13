package sh.calaba.instrumentationbackend.actions.device;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * The class can be used to horizontally scroll to the target element, either scrolling horizontally forward or backward.
 * The command to run horizontally forward example: perform_action('uiautomator_horizontally_scroll_forward_or_backward_to_element', 'FORWARD', 'text', 'Android', '10')
 * The command to run horizontally backward example: perform_action('uiautomator_horizontally_scroll_forward_or_backward_to_element', 'BACKWARD', 'text', 'Android', '10')
 * Documentation for the above can be found at https://github.com/calabash/calabash-android/wiki/UIAutomator2
 */
public class UiautomatorHorizontallyScrollingForwardOrBackward implements Action {
    private final UiautomatorScrollingForwardOrBackward scrollingAction;
    public UiautomatorHorizontallyScrollingForwardOrBackward() {
        this.scrollingAction = new UiautomatorScrollingForwardOrBackward(
                "uiautomator_horizontally_scroll_forward_or_backward_to_element",
                true
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
