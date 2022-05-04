package androidx.test.uiautomator;

public class UiScrollableCustom extends UiScrollable {
    public UiScrollableCustom(UiSelector container) {
        super(container);
    }

    public boolean scrollIntoView(UiSelector selector) throws UiObjectNotFoundException {
        Tracer.trace(new Object[]{selector});
        UiSelector childSelector = this.getSelector().childSelector(selector);
        boolean found = false;

        for (int x = 0; x < getMaxSearchSwipes() && !found; ++x) {
            if (this.exists(childSelector)) {
                found = true;
            } else {
                boolean scrolled = this.scrollForward(100);
                if (!scrolled) break;
            }
        }
        return found;
    }

}
