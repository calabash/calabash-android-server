package android.support.test.uiautomator;

import android.support.test.uiautomator.Tracer;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;

/**
 * Created by rajdeepvarma on 14/12/16.
 */
public class UiScrollableCustom extends UiScrollable {
    public UiScrollableCustom(UiSelector container) {
        super(container);
    }

    public boolean scrollIntoView(UiSelector selector) throws UiObjectNotFoundException {
        Tracer.trace(new Object[]{selector});
        UiSelector childSelector = this.getSelector().childSelector(selector);
        if(this.exists(childSelector)) {
            return true;
        } else {
            if(this.exists(childSelector)) {
                return true;
            } else {
                for(int x = 0; x < getMaxSearchSwipes(); ++x) {
                    boolean scrolled = this.scrollForward(100);
                    if(this.exists(childSelector)) {
                        return true;
                    }

                    if(!scrolled) {
                        return false;
                    }
                }

                return false;
            }
        }
    }

}
