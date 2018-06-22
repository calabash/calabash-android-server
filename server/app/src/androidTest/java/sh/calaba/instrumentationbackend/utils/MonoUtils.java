package sh.calaba.instrumentationbackend.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.lang.reflect.Method;

import sh.calaba.instrumentationbackend.InstrumentationBackend;

public class MonoUtils {
    /*
        @return [Boolean| true if mono was loaded
     */
    public static boolean loadMono(Context context) {
        try {
            Class<?> c = Class.forName("mono.MonoPackageManager");
            String[] strings = {context.getApplicationInfo().sourceDir};
            try {
                // 64bit support
                Method loadApplication = c.getDeclaredMethod("LoadApplication", Context.class, ApplicationInfo.class, String[].class);
                loadApplication.invoke(null, context, context.getApplicationInfo(), strings);
            } catch (NoSuchMethodException e) {
                Method loadApplication = c.getDeclaredMethod("LoadApplication", Context.class, String.class, String[].class);
                loadApplication.invoke(null, context, null, strings);
            }

            System.out.println("Calabash loaded Mono");

            return true;
        } catch (Exception e) {
            System.out.println("Calabash did not load Mono. This is only a problem if you are trying to test a Mono application");

            return false;
        }
    }
}
