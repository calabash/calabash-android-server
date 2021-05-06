package sh.calaba.instrumentationbackend.actions.text;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.utils.StringUtils;
import sh.calaba.instrumentationbackend.utils.SystemPropertiesWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InfoMethodUtil {

    @Nullable
    public static View getInputView() {
        try {
            return getInternalServedView();
        } catch (UnexpectedInputMethodManagerStructureException e) {
            System.err.println("Internal servedView is unavailable: " + StringUtils.toString(e));
        }

        Activity activity = InstrumentationBackend.getCurrentActivity();
        if (activity != null) {
            return activity.getCurrentFocus();
        }
        return null;
    }

    public static InputConnection getInputConnection() throws UnexpectedInputMethodManagerStructureException {
        Context context = InstrumentationBackend.instrumentation.getTargetContext();

        try {
            if (Build.VERSION.SDK_INT > 27) {
                EditorInfo info = new EditorInfo();
                View inputView = getInputView();

                if (inputView != null){
                    return inputView.onCreateInputConnection(info);
                }

                return null;
            }

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
    public static int getTextLength(InputConnection inputConnection) {
        return getTextLength(TextPosition.BEFORE, inputConnection) +
                getTextLength(TextPosition.SELECTED, inputConnection) +
                getTextLength(TextPosition.AFTER, inputConnection);
    }

    public static int getSelectionStart(InputConnection inputConnection) {
        return getTextLength(TextPosition.BEFORE, inputConnection);
    }

    public static int getSelectionEnd(InputConnection inputConnection) {
        return getTextLength(inputConnection) - getTextLength(TextPosition.AFTER, inputConnection);
    }

    private enum TextPosition {
        BEFORE, SELECTED, AFTER
    }

    private static View getInternalServedView() throws UnexpectedInputMethodManagerStructureException {
        Context context = InstrumentationBackend.instrumentation.getTargetContext();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        try {
            Field servedViewField = InputMethodManager.class.getDeclaredField("mServedView");
            servedViewField.setAccessible(true);

            return (View)servedViewField.get(inputMethodManager);
        } catch (IllegalAccessException e) {
            throw new UnexpectedInputMethodManagerStructureException(e);
        } catch (NoSuchFieldException e) {
            try {
                Method method = InputMethodManager.class.getDeclaredMethod("getServedViewLocked");
                method.setAccessible(true);

                return (View)method.invoke(inputMethodManager);
            } catch (NoSuchMethodException e2) {
                throw new UnexpectedInputMethodManagerStructureException(e2);
            } catch (IllegalAccessException e3) {
                throw new UnexpectedInputMethodManagerStructureException(e3);
            } catch (InvocationTargetException e4) {
                throw new UnexpectedInputMethodManagerStructureException(e4);
            }
        }
    }

    private static int getTextLength(TextPosition textPosition, InputConnection inputConnection) {
        int lastLength = 0;

        // We are asked to supply a buffer of size n to hold the characters. Therefore we loop
        // to find the least buffer size needed.
        for (int n = 1; n > 0; n *= 10) {
            CharSequence text;

            switch (textPosition) {
                case BEFORE:
                    text = inputConnection.getTextBeforeCursor(n, 0);
                    break;
                case AFTER:
                    text = inputConnection.getTextAfterCursor(n, 0);
                    break;
                case SELECTED:
                    if (Build.VERSION.SDK_INT >= 9) {
                        text = inputConnection.getSelectedText(0);
                    } else {
                        text = "";
                    }

                    break;
                default:
                    text = null;
            }

            if (text == null) {
                // We have no text in the current position (or an error has occurred)
                return 0;
            }

            int length = text.length();

            if (length == lastLength) {
                return length;
            } else {
                lastLength = length;
            }
        }

        throw new RuntimeException("The text of the current input connection is too big.");
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
