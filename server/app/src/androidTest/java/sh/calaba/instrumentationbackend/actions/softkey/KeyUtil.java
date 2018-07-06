package sh.calaba.instrumentationbackend.actions.softkey;

import android.view.KeyEvent;

import java.lang.reflect.Field;

public class KeyUtil {
    public static Integer getKey(String keyName) {
        keyName = keyName.toUpperCase();

        Class<?> keyEventClass = KeyEvent.class;
        try {
            Field field = keyEventClass.getField(keyName);

            return field.getInt(null);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static boolean isNumber(String string) {
        try {
            Integer.parseInt(string);

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
