package sh.calaba.instrumentationbackend.utils;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class WindowManagerWrapper {
    private WindowManager windowManager;

    public WindowManagerWrapper(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public List<View> getViews() {
        if (Build.VERSION.SDK_INT < 17) {
            try {
                Field viewsField = windowManager.getClass().getDeclaredField("mViews");
                viewsField.setAccessible(true);

                return Arrays.asList((View[]) viewsField.get(windowManager));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new WindowManagerGlobalWrapper(windowManager).getViews();
        }
    }

    private class WindowManagerGlobalWrapper {
        private Object windowManagerGlobal;

        public WindowManagerGlobalWrapper(WindowManager windowManager) {
            try {
                Field globalField = windowManager.getClass().getDeclaredField("mGlobal");
                globalField.setAccessible(true);
                windowManagerGlobal = globalField.get(windowManager);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public List<View> getViews() {
            try {
                Field viewsField = windowManagerGlobal.getClass().getDeclaredField("mViews");
                viewsField.setAccessible(true);


                if (Build.VERSION.SDK_INT >= 19) {
                    return (List) viewsField.get(windowManagerGlobal);
                } else {
                    return Arrays.asList((View[]) viewsField.get(windowManagerGlobal));

                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
