package sh.calaba.instrumentationbackend.actions.device;

/**
 * List of UIAutomator actions supported by uiautomator_execute command.
 *
 * List of available methods in UIAutomator can be found here:
 * https://developer.android.com/reference/androidx/test/uiautomator/UiObject2
 */
public enum Actions {
    click,
    longClick,
    getText,
    getContentDescription,
    getClassName,
    getResourceName,
    getVisibleBounds,
    getVisibleCenter,
    getApplicationPackage,
    getChildCount,
    clear,
    isCheckable,
    isChecked,
    isClickable,
    isEnabled,
    isFocusable,
    isFocused,
    isLongClickable,
    isScrollable,
    isSelected;
}
