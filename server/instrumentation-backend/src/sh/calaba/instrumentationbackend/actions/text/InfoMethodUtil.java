package sh.calaba.instrumentationbackend.actions.text;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.utils.SystemPropertiesWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InfoMethodUtil {
    public static View getServedView() throws UnexpectedInputMethodManagerStructureException {
        Context context = InstrumentationBackend.instrumentation.getTargetContext();

        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            Field servedViewField = InputMethodManager.class.getDeclaredField("mServedView");
            servedViewField.setAccessible(true);

            return (View)servedViewField.get(inputMethodManager);
        } catch (IllegalAccessException e) {
            throw new UnexpectedInputMethodManagerStructureException(e);
        } catch (NoSuchFieldException e) {
            throw new UnexpectedInputMethodManagerStructureException(e);
        }
    }

    public static InputConnection getInputConnection() throws UnexpectedInputMethodManagerStructureException {
        Context context = InstrumentationBackend.instrumentation.getTargetContext();

        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            int previewSdkInt = SystemPropertiesWrapper.getInt("ro.build.version.preview_sdk", 0);

            // We support Android N, which has SDK_INT of 22, but PREVIEW_SDK_INT >= 3
            if (Build.VERSION.SDK_INT > 23
                    || (Build.VERSION.SDK_INT == 23 && previewSdkInt >= 3)) { // Android N changed its internal structure
                Field servedInputConnectionWrapperField = inputMethodManager.getClass().getDeclaredField("mServedInputConnectionWrapper");
                servedInputConnectionWrapperField.setAccessible(true);
                Object servedInputConnectionWrapper = servedInputConnectionWrapperField.get(inputMethodManager);

                Class iInputConnectionWrapperClass = Class.forName("com.android.internal.view.IInputConnectionWrapper");
                Field inputConnectionField = iInputConnectionWrapperClass.getDeclaredField("mInputConnection");
                inputConnectionField.setAccessible(true);

                return (InputConnection) inputConnectionField.get(servedInputConnectionWrapper);
            } else {
                Field servedInputConnectionField = InputMethodManager.class.getDeclaredField("mServedInputConnection");
                servedInputConnectionField.setAccessible(true);

                return (InputConnection) servedInputConnectionField.get(inputMethodManager);
            }
        } catch (IllegalAccessException e) {
            throw new UnexpectedInputMethodManagerStructureException(e);
        } catch (NoSuchFieldException e) {
            throw new UnexpectedInputMethodManagerStructureException(e);
        } catch (ClassNotFoundException e) {
            throw new UnexpectedInputMethodManagerStructureException(e);
        }
    }

    /*
     Find length of non-formatted text
    */
    public static int getEditableTextLength(Editable editable) {
        return TextUtils.substring(editable, 0, editable.length()).length();
    }

    public static Editable getEditable(View view, InputConnection inputConnection) {
        Editable editable = null;

        if (inputConnection instanceof BaseInputConnection) {
            editable = ((BaseInputConnection) inputConnection).getEditable();
        } else if (view instanceof TextView) {
            editable = ((TextView) view).getEditableText();
        } else {
            try {
                Method m = view.getClass().getMethod("getEditableText");
                m.setAccessible(true);
                Object o = m.invoke(view);

                if (o instanceof Editable) {
                    editable = (Editable) o;
                }
            } catch (NoSuchMethodException e) {
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            }
        }

        if (editable == null) {
            throw new IllegalStateException("View '" + view + "' is not editable");
        } else {
            return editable;
        }
    }

    public static int getTextLength(View servedView, InputConnection inputConnection) {
        if ("org.chromium.content.browser.input.ThreadedInputConnection".equals(inputConnection.getClass().getName())) {
            try {
                Field imeAdapterField = inputConnection.getClass().getDeclaredField("mImeAdapter");
                imeAdapterField.setAccessible(true);
                Object imeAdapter = imeAdapterField.get(inputConnection);

                Class<?> imeAdapterClass = inputConnection.getClass().getClassLoader().loadClass("org.chromium.content.browser.input.ImeAdapter");
                Field inputConnectionField = imeAdapterClass.getDeclaredField("mLastText");
                inputConnectionField.setAccessible(true);

                return ((String)inputConnectionField.get(imeAdapter)).length();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            final Editable editable = InfoMethodUtil.getEditable(servedView, inputConnection);

            // Find length of non-formatted text
            return InfoMethodUtil.getEditableTextLength(editable);
        }
    }

    public static class UnexpectedInputMethodManagerStructureException extends Exception {
        public UnexpectedInputMethodManagerStructureException() {
            super();
        }

        public UnexpectedInputMethodManagerStructureException(String detailMessage) {
            super(detailMessage);
        }

        public UnexpectedInputMethodManagerStructureException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public UnexpectedInputMethodManagerStructureException(Throwable throwable) {
            super(throwable);
        }
    }
}
