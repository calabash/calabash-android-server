package sh.calaba.instrumentationbackend.actions.device;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * Created by rajdeepvarma on 04/04/2017.
 */

public class UiautomatorExecute implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();
        String text = null;
        try {

            Method methodName = By.class.getMethod(args[0], String.class);
            BySelector selector = (BySelector) methodName.invoke(By.class, args[1]);

            Method methodOperation = UiObject2.class.getMethod(args[2]);

            UiObject2 object = mDevice.findObject(selector);

            Object res = methodOperation.invoke(object);
            if (res != null) {
                text = res.toString();
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (IllegalAccessException e) {
            return new Result(false, e.getMessage());
        } catch (InvocationTargetException e) {
            return new Result(false, e.getMessage());
        }

        return new Result(true, text);
    }

    @Override
    public String key() {
        return "uiautomator_execute";
    }
}
