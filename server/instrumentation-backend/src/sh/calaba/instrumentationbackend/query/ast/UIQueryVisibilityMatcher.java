package sh.calaba.instrumentationbackend.query.ast;

import android.view.View;

import java.util.Map;

import sh.calaba.instrumentationbackend.query.ViewMapper;
import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.query.ui.UIObjectWebResult;

public class UIQueryVisibilityMatcher extends UIQueryMatcher<Boolean> {
    UIQueryVisibilityMatcher(UIObject uiObject) {
        super(uiObject);
    }

    @Override
    protected Boolean matchForUIObject(UIObjectView uiObjectView) {
        View view = uiObjectView.getObject();

        if (view.getHeight() == 0 || view.getWidth() == 0) {
            return false;
        }

        return view.isShown() && UIQueryUtils.isViewSufficientlyShown(view);
    }

    @Override
    protected Boolean matchForUIObject(UIObjectWebResult uiObjectWebResult) {
        Map<String,Integer> viewRect = uiObjectWebResult.getRect();
        WebContainer webContainer = uiObjectWebResult.getWebContainer();
        Map<String,Integer> parentViewRec = ViewMapper.getRectForView(webContainer.getView());
        return UIQueryUtils.isViewSufficientlyShown(viewRect, parentViewRec);
    }
}
