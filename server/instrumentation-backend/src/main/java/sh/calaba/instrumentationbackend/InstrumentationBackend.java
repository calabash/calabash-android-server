package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.content.Intent;
import sh.calaba.instrumentationbackend.actions.Actions;
import sh.calaba.instrumentationbackend.actions.location.FakeGPSLocation;
import sh.calaba.instrumentationbackend.intenthook.ActivityIntentFilter;
import sh.calaba.instrumentationbackend.intenthook.IIntentHook;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
    private static Map<ActivityIntentFilter, IntentHookWithCount> intentHooks =
            new HashMap<ActivityIntentFilter, IntentHookWithCount>();

    private static CalabashAutomation calabashAutomation;

    /* Instrumentation does not belong to this class. Here because of old architecture */
    public static Instrumentation instrumentation;

    public static SoloEnhanced solo;
    public static Actions actions;


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

    public static void putIntentHook(ActivityIntentFilter activityIntentFilter, IIntentHook intentHook,
                                     int hookUsageCount) {
        Logger.debug("Adding intent hook '" + intentHook + "' for '" + activityIntentFilter + "'");
        intentHooks.put(activityIntentFilter, new IntentHookWithCount(intentHook, hookUsageCount));
    }

    public static void removeIntentHook(ActivityIntentFilter activityIntentFilter) {
        Logger.debug("Removing intent hook for '" + activityIntentFilter + "'");

        IntentHookWithCount intentHookWithCount = intentHooks.get(activityIntentFilter);

        if (intentHookWithCount != null) {
            intentHookWithCount.getIntentHook().onRemoved();
        }

        intentHooks.remove(activityIntentFilter);
    }

    public static ActivityIntentFilter getFilterFor(Intent intent, Activity targetActivity) {
        for (ActivityIntentFilter activityIntentFilter : intentHooks.keySet()) {
            if (activityIntentFilter.match(intent, targetActivity)) {
                return activityIntentFilter;
            }
        }

        return null;
    }

    public static IIntentHook useIntentHookFor(Intent intent, Activity targetActivity) {
        ActivityIntentFilter activityIntentFilter = getFilterFor(intent, targetActivity);

        if (activityIntentFilter == null) {
            return null;
        } else {
            IntentHookWithCount intentHookWithCount = intentHooks.get(activityIntentFilter);

            intentHookWithCount.use();

            if (intentHookWithCount.shouldRemove()) {
                removeIntentHook(activityIntentFilter);
            }

            return intentHookWithCount.getIntentHook();
        }
    }

    public static boolean shouldFilter(Intent intent, Activity targetActivity) {
        ActivityIntentFilter activityIntentFilter = getFilterFor(intent, targetActivity);

        return (activityIntentFilter != null);
    }

    private static class IntentHookWithCount {
        private IIntentHook intentHook;
        private int count;

        private IntentHookWithCount(IIntentHook intentHook, int count) {
            this.intentHook = intentHook;
            this.count = count;
        }

        public IIntentHook getIntentHook() {
            return intentHook;
        }

        public int getCount() {
            return count;
        }

        public void use() {
            if (count != -1) {
                count--;
            }
        }

        public boolean shouldRemove() {
            return (count == 0);
        }
    }
}
