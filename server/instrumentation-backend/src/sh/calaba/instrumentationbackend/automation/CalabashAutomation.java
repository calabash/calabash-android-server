package sh.calaba.instrumentationbackend.automation;

import android.app.Activity;
import sh.calaba.instrumentationbackend.query.ui.UIObject;

import java.util.Collection;

public interface CalabashAutomation {
    public Activity getCurrentActivity();
    public Collection<? extends UIObject> getRootViews();
}
