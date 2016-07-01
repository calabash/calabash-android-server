package sh.calaba.instrumentationbackend.entrypoint;

import android.app.Activity;
import android.app.Application;
import sh.calaba.instrumentationbackend.CalabashInstrumentation;
import sh.calaba.instrumentationbackend.automation.ApplicationUnderTest;

import java.lang.ref.WeakReference;
import java.util.Iterator;

public final class ApplicationUnderTestInstrumentation implements ApplicationUnderTest {
    private final CalabashInstrumentation calabashInstrumentation;

    public ApplicationUnderTestInstrumentation(CalabashInstrumentation calabashInstrumentation) {
        this.calabashInstrumentation = calabashInstrumentation;
    }

    @Override
    public Application getApplication() {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            return currentActivity.getApplication();
        } else {
            return null;
        }
    }

    @Override
    public Activity getCurrentActivity() {
        return calabashInstrumentation.getLastActivity().get();
    }
}