package sh.calaba.instrumentationbackend.query.ast;

import java.util.concurrent.Callable;

import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.query.ui.UIObjectWebResult;

public abstract class UIQueryMatcher<T> implements Callable<T> {
    private final UIObject uiObject;

    UIQueryMatcher(UIObject uiObject) {
        this.uiObject = uiObject;
    }

    protected UIObject getUiObject() {
        return uiObject;
    }

    @Override
    final public T call() throws Exception {
        if (uiObject instanceof UIObjectView) {
            return matchForUIObject((UIObjectView) uiObject);
        } else if (uiObject instanceof UIObjectWebResult) {
            return matchForUIObject((UIObjectWebResult) uiObject);
        }

        if (uiObject == null) {
            throw new InvalidUIQueryException("Invalid UIObject, cannot be null");
        }

        throw new InvalidUIQueryException("Invalid UIObject '" + uiObject.getClass() + "'");
    }

    protected abstract T matchForUIObject(UIObjectView uiObjectView);
    protected abstract T matchForUIObject(UIObjectWebResult uiObjectWebResult);
}
