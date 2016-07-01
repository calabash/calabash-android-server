package sh.calaba.instrumentationbackend.automation;

import android.app.Activity;
import android.app.Application;
import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;

import java.util.List;

/*
    Central component. Handles automation for an application running in a single process.
 */
public class CalabashAutomationEmbedded implements CalabashAutomation {
    private final ApplicationUnderTest applicationUnderTest;

    public CalabashAutomationEmbedded(ApplicationUnderTest applicationUnderTest) {
        this.applicationUnderTest = applicationUnderTest;
    }

    @Override
    public Activity getCurrentActivity() {
        return applicationUnderTest.getCurrentActivity();
    }

    @Override
    public Application getCurrentApplication() {
        return applicationUnderTest.getApplication();
    }

    @Override
    public List<UIObjectView> getRootViews() {
        return UIObjectView.listOfUIObjects(UIQueryUtils.getRootViews());
    }
}
