package sh.calaba.instrumentationbackend;

import android.app.Activity;
import sh.calaba.instrumentationbackend.actions.Actions;
import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.util.Log;

import com.jayway.android.robotium.solo.SoloEnhanced;
import sh.calaba.instrumentationbackend.automation.CalabashAutomation;
import sh.calaba.instrumentationbackend.query.ui.UIObject;

import java.lang.ref.WeakReference;
import java.util.Collection;

/*
    Utility class based on the current test-server life cycle.
 */
public class InstrumentationBackend {
    private static final String TAG = "InstrumentationBackend";

    private static CalabashAutomation calabashAutomation;

    public static synchronized void setDefaultCalabashAutomation(CalabashAutomation calabashAutomation) {
        InstrumentationBackend.calabashAutomation = calabashAutomation;
    }

    public static synchronized CalabashAutomation getDefaultCalabashAutomation() {
        return calabashAutomation;
    }

    public static Activity getCurrentActivity() {
        return getDefaultCalabashAutomation().getCurrentActivity();
    }

    public static Collection<? extends UIObject> getRootViews() {
        return getDefaultCalabashAutomation().getRootViews();
    }

    public static void log(String message) {
        Log.i(TAG, message);
    }

    public static void logError(String message) {
        Log.e(TAG, message);
    }

    /* Instrumentation does not belong to this class. Here because of old architecture */
    public static Instrumentation instrumentation;

    public static SoloEnhanced solo;
    public static Actions actions;

    public static void tearDown() {
        System.out.println("Finishing");

        try {
            solo.finishOpenedActivities();
            solo.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        removeTestLocationProviders(instrumentation.getTargetContext());
    }

    private static void removeTestLocationProviders(Context context) {
        int hasPermission = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_MOCK_LOCATION);

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationService = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            for (final String provider : locationService.getAllProviders()) {
                locationService.removeTestProvider(provider);
            }
        }
    }
}
