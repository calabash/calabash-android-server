package sh.calaba.instrumentationbackend.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemPropertiesWrapper {
    public static int getInt(String key, int def) {
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method getInt = systemPropertiesClass.getMethod("getInt", String.class, int.class);

            return (Integer) getInt.invoke(null, key, def);
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
}
