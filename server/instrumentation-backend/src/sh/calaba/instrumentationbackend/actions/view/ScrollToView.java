package sh.calaba.instrumentationbackend.actions.view;

import android.graphics.Rect;
import android.view.View;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;
import sh.calaba.instrumentationbackend.query.ViewMapper;
import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.instrumentationbackend.query.ast.UIQueryMatcher;
import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.query.ui.UIObjectWebResult;

import java.util.List;
import java.util.Map;

/**
 * Bring views identified by query onto screen
 */

public class ScrollToView implements Action, IOnViewAction {
    @Override
    public Result execute(String... args) {
        ExecuteOnView executeOnView = new ExecuteOnView();
        return executeOnView.execute(this, args);
    }

    @Override
    public String key() {
        return "scroll_to_view";
    }

    private void scrollTo(final View view) {
            UIQueryUtils.runOnViewThread(view, new Runnable() {
                @Override
                public void run() {
                    View rootView = view.getRootView();
                    Rect rect = new Rect();

                    if (view.getWidth() < rootView.getWidth()) { // smaller than parent
                        rect.left = 0;
                        rect.right = view.getWidth();
                    } else {
                        int delta = view.getWidth() - rootView.getWidth();
                        rect.left = delta / 2;
                        rect.right = rect.left + rootView.getWidth();
                    }

                    if (view.getHeight() < rootView.getHeight()) { // smaller than parent
                        rect.top = 0;
                        rect.bottom = view.getHeight();
                    } else {
                        int delta = view.getHeight() - rootView.getHeight();
                        rect.top = delta / 2;
                        rect.bottom = rect.top + view.getHeight();
                    }
                    view.requestRectangleOnScreen(rect, true);

                }
            });
        }

    private void scrollTo(Map viewMap, WebContainer webContainer) {
            final View view = webContainer.getView();
            Map rectMap = (Map) viewMap.get("rect");
            final int x = (Integer)rectMap.get("x");
            final int y = (Integer)rectMap.get("y");
            final int height = (Integer)rectMap.get("height");
            final int width = (Integer) rectMap.get("width");

            UIQueryUtils.runOnViewThread(view, new Runnable() {
                @Override
                public void run() {
                    int[] webViewLocation = UIQueryUtils.getViewLocationOnScreen(view);

                    int webViewHeight = view.getHeight();
                    int webViewWidth = view.getWidth();
                    int webviewY = webViewLocation[1];
                    int webviewX = webViewLocation[0];

                    int offsetY;
                    if(webViewHeight > height) {
                        int webviewBottom = webviewY + webViewHeight;
                        int elementBottom = y + height;
                        if(elementBottom > webviewBottom) {
                            offsetY = elementBottom - webviewBottom;
                        } else if(y < webviewY) {
                            offsetY = y - webviewY;
                        } else {
                            offsetY = 0;
                        }
                    } else {
                        int delta = height - webViewHeight;
                        offsetY = y-webviewY + delta/2;
                    }

                    int offsetX;
                    if(webViewWidth > width) {
                        int webviewRight = webviewX + webViewWidth;
                        int elementRight = x + width;
                        if(elementRight > webviewRight) {
                            offsetX = elementRight - webviewRight;
                        } else if(x < webviewX) {
                            offsetX = x - webviewX;
                        } else {
                            offsetX = 0;
                        }
                    } else {
                        int delta = width - webViewWidth;
                        offsetX = x-webviewX + delta/2;
                    }
                    view.scrollBy(offsetX, offsetY);
                }
            });
        }

    @Override
    public UIQueryMatcher<String> getUIQueryMatcher(UIObject uiObject) {
        return new Mapper(uiObject);
    }

    private class Mapper extends UIQueryMatcher<String> {
        Mapper(UIObject uiObject) {
            super(uiObject);
        }

        @Override
        protected String matchForUIObject(UIObjectView uiObjectView) {
            ScrollToView.this.scrollTo(uiObjectView.getObject());

            return "success";
        }

        @Override
        protected String matchForUIObject(UIObjectWebResult uiObjectWebResult) {
            ScrollToView.this.scrollTo(uiObjectWebResult.getObject(),
                    uiObjectWebResult.getWebContainer());

            return "success";
        }
    }
}
