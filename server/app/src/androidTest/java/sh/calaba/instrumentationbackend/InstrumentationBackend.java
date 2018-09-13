package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.content.Intent;
import sh.calaba.instrumentationbackend.actions.Actions;
import sh.calaba.instrumentationbackend.actions.location.FakeGPSLocation;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import com.jayway.android.robotium.solo.SoloEnhanced;
import sh.calaba.instrumentationbackend.automation.CalabashAutomation;
import sh.calaba.instrumentationbackend.query.ui.UIObject;

import java.util.*;

/*
    Utility class based on the current test-server life cycle.
 */
public class InstrumentationBackend {
    private static final String TAG = "InstrumentationBackend";

    public static List<Intent> intents = new ArrayList<Intent>();

    private static CalabashAutomation calabashAutomation;

    /* Instrumentation does not belong to this class. Here because of old architecture */
    public static Instrumentation instrumentation;

    public static SoloEnhanced solo;
    public static Actions actions;
    public static UiDevice uiDevice;


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

}
