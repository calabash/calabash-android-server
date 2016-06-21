package sh.calaba.instrumentationbackend.utils;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import sh.calaba.instrumentationbackend.CalabashInstrumentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActivityThreadWrapper {
    public final static Class<?> activityThreadClass;

    static {
        Class<?> activityThreadClassT = null;

        try {
            activityThreadClassT = Class.forName("android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
        }

        activityThreadClass = activityThreadClassT;
    }

    private final Object activityThread;

    public static ActivityThreadWrapper fromCurrentActivityThread() {
        try {
            return new ActivityThreadWrapper(activityThreadClass.getMethod("currentActivityThread").invoke(null));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public ActivityThreadWrapper(Object activityThread) {
        this.activityThread = activityThread;
    }

    public Object getActivityThread() {
        return activityThread;
    }

    public ApplicationInfo getCurrentApplicationInfo() {
        try {
            Object boundApplication = getBoundApplication();

            Field appInfoField = boundApplication.getClass().getDeclaredField("appInfo");
            appInfoField.setAccessible(true);

            return (ApplicationInfo) appInfoField.get(boundApplication);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getCurrentInfo() {
        try {
            Object boundApplication = getBoundApplication();

            Field appInfoField = boundApplication.getClass().getDeclaredField("info");
            appInfoField.setAccessible(true);

            return appInfoField.get(boundApplication);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public Context getSystemContext() {
        try {
            Method getSystemContextMethod = activityThreadClass.getMethod("getSystemContext");

            return (Context) getSystemContextMethod.invoke(activityThread);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Object getLoadedApk(ApplicationInfo applicationInfo) {
        /*
        LoadedApk pi = getPackageInfo(instrApp, data.compatInfo,
                    appContext.getClassLoader(), false, true, false);
        LoadedApk pi = getPackageInfo(instrApp, data.compatInfo,
                    appContext.getClassLoader(), false, true);
        LoadedApk pi = getPackageInfo(instrApp,
                    appContext.getClassLoader(), false, true);
         */
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                Class compatibilityInfoClass = Class.forName("android.content.res.CompatibilityInfo");
                Constructor compatibilityInfoConstructor = compatibilityInfoClass.getDeclaredConstructor(
                        ApplicationInfo.class, int.class, int.class, boolean.class);
                compatibilityInfoConstructor.setAccessible(true);
                Object compatibilityInfo = compatibilityInfoConstructor.newInstance(applicationInfo, 0x04, 0, false);

                if (Build.VERSION.SDK_INT >= 20) {
                    Method m = activityThreadClass.getDeclaredMethod("getPackageInfo", ApplicationInfo.class, compatibilityInfoClass,
                            ClassLoader.class, boolean.class, boolean.class, boolean.class);
                    m.setAccessible(true);

                    return m.invoke(activityThread, applicationInfo, compatibilityInfo, getClass().getClassLoader(), false, true, false);
                } else {
                    Method m = activityThreadClass.getDeclaredMethod("getPackageInfo", ApplicationInfo.class, compatibilityInfoClass,
                            ClassLoader.class, boolean.class, boolean.class);
                    m.setAccessible(true);

                    return m.invoke(activityThread, applicationInfo, compatibilityInfo, getClass().getClassLoader(), false, true);
                }
            } else {
                Method m = activityThreadClass.getDeclaredMethod("getPackageInfo", ApplicationInfo.class,
                        ClassLoader.class, boolean.class, boolean.class);
                m.setAccessible(true);

                return m.invoke(activityThread, applicationInfo, getClass().getClassLoader(), false, true);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        try {
            Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            instrumentationField.setAccessible(true);
            instrumentationField.set(getActivityThread(), instrumentation);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getBoundApplication() {
        try {
            Field boundApplicationField = activityThread.getClass().getDeclaredField("mBoundApplication");
            boundApplicationField.setAccessible(true);

            return boundApplicationField.get(activityThread);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
