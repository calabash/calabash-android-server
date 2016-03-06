package sh.calaba.instrumentationbackend.actions.view;

import sh.calaba.instrumentationbackend.query.ast.UIQueryMatcher;
import sh.calaba.instrumentationbackend.query.ui.UIObject;

/**
 * Created by john7doe on 06/01/15.
 */
public interface IOnViewAction {
    UIQueryMatcher<String> getUIQueryMatcher(UIObject uiObject);
}
