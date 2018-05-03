package sh.calaba.instrumentationbackend.actions.view;

import android.view.View;
import android.webkit.WebView;

import java.io.IOException;
import java.util.Map;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;
import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.instrumentationbackend.query.ast.UIQueryMatcher;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.query.ui.UIObjectWebResult;
import sh.calaba.org.codehaus.jackson.map.ObjectMapper;

public class ViewConfiguration implements Action, IOnViewAction {
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public Result execute(String... args) {
        ExecuteOnView executeOnView = new ExecuteOnView();
        return executeOnView.execute(this, args);
    }

    public String doOnView(View view) {
        android.view.ViewConfiguration result = android.view.ViewConfiguration.get(view.getContext());

        try {
            return mapper.writeValueAsString(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String key() {
        return "view_configuration";
    }

    @Override
    public UIQueryMatcher<String> getUIQueryMatcher(UIObject uiObject) {
        return new Mapper(uiObject);
    }

    private class Mapper extends UIQueryMatcher<String> {
        public Mapper(UIObject uiObject) {
            super(uiObject);
        }

        @Override
        protected String matchForUIObject(UIObjectView uiObjectView) {
            return ViewConfiguration.this.doOnView(uiObjectView.getObject());
        }

        @Override
        protected String matchForUIObject(UIObjectWebResult uiObjectWebResult) {
            return ViewConfiguration.this.doOnView(uiObjectWebResult.getWebContainer().getView());
        }
    }
}
