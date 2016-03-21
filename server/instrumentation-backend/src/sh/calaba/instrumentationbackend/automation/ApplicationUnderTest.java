package sh.calaba.instrumentationbackend.automation;

import android.app.Activity;
import android.app.Application;

import java.lang.ref.WeakReference;

public interface ApplicationUnderTest {
    public WeakReference<Application> getApplication();
    public WeakReference<Activity> getCurrentActivity();
}
