package sh.calaba.instrumentationbackend.automation;

import android.app.Activity;
import android.app.Application;

public interface ApplicationUnderTest {
    public Application getApplication();
    public Activity getCurrentActivity();
}
