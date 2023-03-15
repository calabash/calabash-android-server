package sh.calaba.instrumentationbackend.actions.device;
import static sh.calaba.instrumentationbackend.actions.device.StrategyUtils.verifyStrategy;

import android.graphics.Rect;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class UiautomatorSeekBar implements Action{
    @Override
    public Result execute(String... args) {
        UiDevice device = InstrumentationBackend.getUiDevice();
        String error_message =  "null";

        try {
            String strategy = args[0];
            String locator = args[1];

            verifyStrategy(strategy);

            Method strategyMethod = By.class.getMethod(strategy, String.class);
            BySelector selector = (BySelector) strategyMethod.invoke(By.class, locator);
            UiObject2 seekBar = device.findObject(selector);

            String from_direction = args[2];
            String to_direction = args[3];
            Rect bounds = seekBar.getVisibleBounds();

            int startX = bounds.left + bounds.width() / 4;
            int endX = bounds.left + bounds.width() * 3 / 4;
            int centerY = bounds.centerY();

            if (Objects.equals(from_direction, "left") && Objects.equals(to_direction, "right")){
                device.swipe(endX, centerY, startX, centerY, 10);
            }
            else if (Objects.equals(from_direction, "right") && Objects.equals(to_direction, "left")){
                device.swipe(startX, centerY, endX, centerY, 10);
            } else {
                error_message= "Incorrect options check the arguments";
            }

        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if(String.valueOf(error_message) != "null") {
            return new Result(true, error_message);
        } else
        {
            return new Result(true);
        }

    }

    @Override
    public String key() {
        return "uiautomator_seek_bar";
    }
}
