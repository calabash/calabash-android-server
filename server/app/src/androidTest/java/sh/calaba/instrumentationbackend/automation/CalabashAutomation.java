package sh.calaba.instrumentationbackend.automation;

import android.app.Activity;
import android.app.Application;
import sh.calaba.instrumentationbackend.query.ui.UIObject;

import java.util.Collection;

public interface CalabashAutomation {
    public Activity getCurrentActivity();
    public Application getCurrentApplication();
    public Collection<? extends UIObject> getRootViews();
}
