package sh.calaba.instrumentationbackend;

import android.app.Activity;
import sh.calaba.instrumentationbackend.actions.Actions;
import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.jayway.android.robotium.solo.PublicViewFetcher;
import com.jayway.android.robotium.solo.SoloEnhanced;

public class InstrumentationBackend {
    public static String testPackage;
    public static String mainActivityName;
    public static Bundle extras;

    private static final String TAG = "InstrumentationBackend";

    /* Instrumentation does not belong to this class. Here because of old architecture */
    public static Instrumentation instrumentation;

    public static SoloEnhanced solo;
    public static Actions actions;
    public static Instrumentation.ActivityMonitor activityMonitor;

    public static Activity getCurrentActivity() {
        return activityMonitor.getLastActivity();
    }

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

    public static void log(String message) {
        Log.i(TAG, message);
    }

    public static void logError(String message) {
        Log.e(TAG, message);
    }

    public static void removeTestLocationProviders(Context context) {
        int hasPermission = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_MOCK_LOCATION);

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationService = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            for (final String provider : locationService.getAllProviders()) {
                locationService.removeTestProvider(provider);
            }
        }
    }
}
