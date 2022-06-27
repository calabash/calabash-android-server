package androidx.test.uiautomator;

public class UiScrollableCustom extends UiScrollable {
    public UiScrollableCustom(UiSelector container) {
        super(container);
    }

    public boolean scrollIntoView(UiSelector selector) throws UiObjectNotFoundException {
        return scrollIntoView(selector, false);
    }

    /**
     * Method required because scrollForward return type is not reliable. It might return false, meaning that there is no
     * more space to scroll while it is not true. This method allows you to force to scroll even if scrollForward returns
     * false. When forcing to scroll, the method will stop scrolling when mMaxSearchSwipes is reached.
     */
    public boolean scrollIntoView(UiSelector selector, boolean forceScroll) throws UiObjectNotFoundException {
        Tracer.trace(new Object[]{selector});
        UiSelector childSelector = this.getSelector().childSelector(selector);
        boolean found = false;

        for (int x = 0; x < getMaxSearchSwipes() && !found; ++x) {
            if (this.exists(childSelector)) {
                found = true;
            } else {
                boolean scrolled = this.scrollForward(100);
                if (!forceScroll && !scrolled) break;
            }
        }
        return found;
    }
}
