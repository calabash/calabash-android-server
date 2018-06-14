package sh.calaba.instrumentationbackend.utils;

import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WindowManagerGlobalWrapper extends WindowManagerWrapper {
    private Object windowManagerGlobal;

    protected WindowManagerGlobalWrapper(WindowManager windowManager) {
        if (windowManager != null) {
            try {
                Field globalField = windowManagerImplClass.getDeclaredField("mGlobal");
                globalField.setAccessible(true);
                windowManagerGlobal = globalField.get(windowManager);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            windowManagerGlobal = getDefaultWindowManager();
        }
    }

    private Object getDefaultWindowManager() {
        try {
            Class<?> windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal");
            Method getDefaultMethod = windowManagerGlobalClass.getMethod("getInstance");

            return getDefaultMethod.invoke(null);
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

    @SuppressWarnings("unchecked")
    public List<View> getViews() {
        try {
            Field viewsField = Class.forName("android.view.WindowManagerGlobal").getDeclaredField("mViews");
            viewsField.setAccessible(true);
            Object views = viewsField.get(windowManagerGlobal);

            if (views == null) {
                return new ArrayList<View>();
            }

            if (Build.VERSION.SDK_INT < 19) {
                return Arrays.asList((View[]) views);
            } else {
                return (List) views;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
