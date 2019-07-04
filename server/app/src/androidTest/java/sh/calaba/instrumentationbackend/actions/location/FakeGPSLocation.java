package sh.calaba.instrumentationbackend.actions.location;


import android.os.Build;
import android.os.SystemClock;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class FakeGPSLocation implements Action {
    // Not present in newer SDK, copied here so that we are not forcing users to download a old SDK just to be able to compile
    private static final String ACCESS_MOCK_LOCATION = "android.permission.ACCESS_MOCK_LOCATION";
    private static final String TEST_PROVIDER = "calabashTestProvider";
    private static final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
    private static ScheduledFuture<?> task;

    public static void stopLocationMocking() {
        executorService.shutdownNow();
        LocationManager locationManager = (LocationManager) InstrumentationBackend.instrumentation.getTargetContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeTestProvider(TEST_PROVIDER);
    }

    @Override
    public Result execute(String... args) {
        final double latitude = Double.parseDouble(args[0]);
        final double longitude = Double.parseDouble(args[1]);


        Context context = InstrumentationBackend.instrumentation.getTargetContext();

        try {
            if (Build.VERSION.SDK_INT <= 22) {
                if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION) != 1) {
                    return Result.failedResult("Allow mock location is not enabled.");
                }
                if (context.checkCallingOrSelfPermission(ACCESS_MOCK_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return Result.failedResult("The application does not have access mock location permission. Add the permission '" + ACCESS_MOCK_LOCATION + "' to your manifest");
                }
            }
        } catch (Settings.SettingNotFoundException e) {
            return Result.failedResult(e.getMessage());
        }

        // Stop any existing location mocking task
        if(task != null) {
            task.cancel(true);
            task = null;
        }

        LocationManager locationManager = (LocationManager) InstrumentationBackend.instrumentation.getTargetContext().getSystemService(Context.LOCATION_SERVICE);
        LocationProvider provider = locationManager.getProvider(TEST_PROVIDER);
        if(provider == null) {
            locationManager.addTestProvider(TEST_PROVIDER, false, false, false, false, true, true, true, 1, 1);
            locationManager.setTestProviderEnabled(TEST_PROVIDER, true);
        }

        task = executorService.scheduleWithFixedDelay(new LocationProviderRunnable(latitude, longitude, locationManager), 0, 500, TimeUnit.MILLISECONDS);
        return Result.successResult();
    }

    private static class LocationProviderRunnable implements Runnable {
        private final double latitude;
        private final double longitude;
        private final LocationManager locationManager;

        LocationProviderRunnable(double latitude, double longitude, LocationManager locationManager) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.locationManager = locationManager;
        }

        @Override
        public void run() {
            final List<String> providerNames = locationManager.getProviders(true);
            System.out.println("Mocking location to: (" + latitude + ", " + longitude + ")");

            for (String providerName : providerNames) {
                if (locationManager.getProvider(providerName) != null) {
                    setLocation(locationManager, providerName, latitude, longitude);
                }
            }
        }

        private void setLocation(LocationManager locationManager, String locationProvider, double latitude, double longitude) {
            Location location = new Location(locationProvider);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(0.1f);
            location.setTime(System.currentTimeMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            locationManager.setTestProviderLocation(TEST_PROVIDER, location);
        }
    }

    @Override
    public String key() {
        return "set_gps_coordinates";
    }

}
