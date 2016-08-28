package sh.calaba.instrumentationbackend.utils;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public abstract class WindowManagerWrapper {
    protected static final Class<?> windowManagerImplClass;

    static {
        try {
            windowManagerImplClass = Class.forName("android.view.WindowManagerImpl");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static WindowManagerWrapper fromContext(Context context) {
        WindowManager windowManager = getWindowManagerFromContext(context);

        if (Build.VERSION.SDK_INT < 17) {
            return new WindowManagerImplWrapper(windowManager);
        } else {
            return new WindowManagerGlobalWrapper(windowManager);
        }
    }

    private static WindowManager getWindowManagerFromContext(Context context) {
        WindowManager windowManager;

        try {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        } catch (IllegalStateException e) {
            // This exception will be thrown if the system service is requested before the onCreate
            // method of the activity is called. This is fine, we should just not use the window
            // manager of this particular activity, but use the default one.
            windowManager = null;
        }

        if (windowManager != null
            && WindowManagerWrapper.windowManagerImplClass.isAssignableFrom(windowManager.getClass())) {
            return windowManager;
        } else {
            return null;
        }
    }

    public abstract List<View> getViews();
}
