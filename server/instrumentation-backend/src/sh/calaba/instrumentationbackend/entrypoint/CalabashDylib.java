package sh.calaba.instrumentationbackend.entrypoint;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import sh.calaba.instrumentationbackend.*;
import sh.calaba.instrumentationbackend.actions.Actions;
import sh.calaba.instrumentationbackend.actions.HttpServer;
import sh.calaba.instrumentationbackend.automation.CalabashAutomation;
import sh.calaba.instrumentationbackend.automation.CalabashAutomationEmbedded;
import sh.calaba.instrumentationbackend.utils.ActivityThreadWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Entry point for calabash being dynamically loaded, for example by a dexclassloader,
 * inside an application from the initializer of `Application`.
 */
public class CalabashDylib implements EntryPoint {
    private final ActivityThreadWrapper activityThreadWrapper;
    private final ApplicationInfo applicationInfo;
    private final Integer testServerPort;
    private final CalabashInstrumentation instrumentation;

    public static void load(int testServerPort) {
        Main.start(new CalabashDylib(testServerPort));
    }

    private static void loadWithoutListeningOnPort() {
        Main.start(new CalabashDylib(null));
    }

    public static class Factory {
        public static EntryPoint newInstance(int testServerPort) {
            return new CalabashDylib(testServerPort);
        }
    }

    private CalabashDylib(Integer testServerPort) {
        this.activityThreadWrapper = ActivityThreadWrapper.fromCurrentActivityThread();
        this.applicationInfo = activityThreadWrapper.getCurrentApplicationInfo();
        this.testServerPort = testServerPort;
        this.instrumentation = new CalabashInstrumentation();
    }

    @Override
    public void start() {
        String testServerPackage = applicationInfo.packageName + ".test";
        ApplicationInfo instrumentationInfo = getApplicationInfoForPackage(testServerPackage);
        Object instrumentationLoadedApk = activityThreadWrapper.getLoadedApk(instrumentationInfo);

        Context instrumentationContext = createAppContext(instrumentationLoadedApk);
        Context appContext = createAppContext(activityThreadWrapper.getCurrentInfo());

        initInstrumentation(instrumentation, activityThreadWrapper.getActivityThread(), instrumentationContext,
                appContext, new ComponentName(instrumentationInfo.packageName, CalabashInstrumentation.class.getName()));

        activityThreadWrapper.setInstrumentation(instrumentation);

        InstrumentationBackend.instrumentation = instrumentation;
        InstrumentationBackend.actions = new Actions(instrumentation);

        if (testServerPort == null) {
            System.out.println("Dylib instantiating server. Not listening at any port!");
            HttpServer.instantiate();
        } else {
            System.out.println("Dylib instantiating server at " + testServerPort);
            HttpServer.instantiate(testServerPort);
        }

        final InstrumentationApplicationLifeCycle applicationLifeCycle =
                new InstrumentationApplicationLifeCycle(instrumentation, null);

        final HttpTestServerLifeCycle testServerLifeCycle =
                new HttpTestServerLifeCycle(HttpServer.getInstance(), applicationLifeCycle);

        testServerLifeCycle.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (testServerLifeCycle.isHttpServerRunning()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("Thread is interrupted, breaking.");
                        break;
                    }
                }

                instrumentation.runOnMainSync(new Runnable() {
                    @Override
                    public void run() {
                        testServerLifeCycle.stop();
                        InstrumentationBackend.tearDown();
                    }
                });
            }
        }).start();
    }

    @Override
    public CalabashAutomation getCalabashAutomation() {
        return new CalabashAutomationEmbedded(
                new ApplicationUnderTestInstrumentation(this.instrumentation));
    }

    private void initInstrumentation(Instrumentation instrumentation, Object activityThread,
                                     Context instrumentationContext, Context appContext, ComponentName component) {
        try {
            Class iInstrumentationWatcherClass = Class.forName("android.app.IInstrumentationWatcher");
            Method initMethod;

            if (Build.VERSION.SDK_INT >= 18) {
                Class iUiAutomationConnectionClass = Class.forName("android.app.IUiAutomationConnection");

                initMethod = Instrumentation.class.getDeclaredMethod("init", ActivityThreadWrapper.activityThreadClass,
                        Context.class, Context.class, ComponentName.class, iInstrumentationWatcherClass,
                        iUiAutomationConnectionClass);
                initMethod.setAccessible(true);
                initMethod.invoke(instrumentation, activityThread, instrumentationContext, appContext,
                        component, null, null);
            } else {
                initMethod = Instrumentation.class.getDeclaredMethod("init", ActivityThreadWrapper.activityThreadClass,
                        Context.class, Context.class, ComponentName.class, iInstrumentationWatcherClass);
                initMethod.setAccessible(true);
                initMethod.invoke(instrumentation, activityThread, instrumentationContext, appContext,
                        component, null);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Context createAppContext(Object loadedApk) {
        String os = android.os.Build.VERSION.RELEASE;
        String patch;

        if (os.length() > 3) {
            patch = os.substring(os.length() - 1);
        } else {
            patch = "0";
        }

        if (Build.VERSION.SDK_INT > 19 ||
                (Build.VERSION.SDK_INT == 19 && ("4".equals(patch) || "3".equals(patch)))) {
            return createAppContextKitKatMR2Plus(loadedApk);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            return createAppContextKitKatMR1Minus(loadedApk);
        } else {
            return createAppContextFroyoMinus(loadedApk);
        }
    }

    private Context createAppContextFroyoMinus(Object packageInfo) {
        // On Froyo, there is no such thing as a LoadedApk. AppBindData's field
        // info is of type "PackageInfo" NOT LoadedApk
        try {
            /*
                ContextImpl appContext = new ContextImpl();
                appContext.init(data.info, null, this);
             */
            Class contextImplClass = Class.forName("android.app.ContextImpl");
            Constructor contextImplConstructor = contextImplClass.getDeclaredConstructor();
            contextImplConstructor.setAccessible(true);
            Context contextImpl = (Context) contextImplConstructor.newInstance();

            Method initMethod = contextImplClass.getDeclaredMethod("init",
                    Class.forName("android.app.ActivityThread$PackageInfo"), IBinder.class, ActivityThreadWrapper.activityThreadClass);
            initMethod.setAccessible(true);
            initMethod.invoke(contextImpl, packageInfo, null, activityThreadWrapper.getActivityThread());

            return contextImpl;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private Context createAppContextKitKatMR1Minus(Object loadedApk) {
        try {
            /*
                new ContextImpl();
                appContext.init(data.info, null, this);
             */
            Class contextImplClass = Class.forName("android.app.ContextImpl");
            Constructor contextImplConstructor = contextImplClass.getDeclaredConstructor();
            contextImplConstructor.setAccessible(true);
            Context contextImpl = (Context) contextImplConstructor.newInstance();

            Method initMethod = contextImplClass.getDeclaredMethod("init",
                    Class.forName("android.app.LoadedApk"), IBinder.class, ActivityThreadWrapper.activityThreadClass);
            initMethod.setAccessible(true);
            initMethod.invoke(contextImpl, loadedApk, null, activityThreadWrapper.getActivityThread());

            return contextImpl;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private Context createAppContextKitKatMR2Plus(Object loadedApk) {
        try {
            Class contextImplClass = Class.forName("android.app.ContextImpl");
            Method createAppContextMethod = contextImplClass.getDeclaredMethod("createAppContext",
                    ActivityThreadWrapper.activityThreadClass, Class.forName("android.app.LoadedApk"));
            createAppContextMethod.setAccessible(true);

            return (Context) createAppContextMethod.invoke(null,
                    activityThreadWrapper.getActivityThread(), loadedApk);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private PackageManager getPackageManager() {
        return activityThreadWrapper.getSystemContext().getPackageManager();
    }

    private ApplicationInfo getApplicationInfoForPackage(String packageName) {
        for (ApplicationInfo info : getPackageManager().getInstalledApplications(0)) {
            if (info.packageName.equals(packageName)) {
                return info;
            }
        }

        return null;
    }
}
