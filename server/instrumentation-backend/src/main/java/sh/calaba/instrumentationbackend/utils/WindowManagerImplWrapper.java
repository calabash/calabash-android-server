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

public class WindowManagerImplWrapper extends   WindowManagerWrapper {
    private WindowManager windowManager;

    protected WindowManagerImplWrapper(WindowManager windowManager) {
        if (windowManager == null) {
            this.windowManager = getDefaultWindowManager();
        } else {
            this.windowManager = windowManager;
        }
    }

    private WindowManager getDefaultWindowManager() {
        try {
            Method getDefaultMethod = windowManagerImplClass.getMethod("getDefault");

            return (WindowManager) getDefaultMethod.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<View> getViews() {
        if (Build.VERSION.SDK_INT < 17) {
            try {
                Field viewsField = windowManagerImplClass.getDeclaredField("mViews");
                viewsField.setAccessible(true);
                Object views = viewsField.get(windowManager);

                if (views == null) {
                    return new ArrayList<View>();
                }

                return Arrays.asList((View[]) views);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new WindowManagerGlobalWrapper(windowManager).getViews();
        }
    }
}
