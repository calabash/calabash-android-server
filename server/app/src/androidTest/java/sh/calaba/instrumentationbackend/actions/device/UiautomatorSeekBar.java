package sh.calaba.instrumentationbackend.actions.device;
import static sh.calaba.instrumentationbackend.actions.device.StrategyUtils.verifyStrategy;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class UiautomatorSeekBar implements Action{
    @Override
    public Result execute(String... args) {
        UiDevice device = InstrumentationBackend.getUiDevice();
        int speed = 10;

        try {
            String strategy = args[0];
            String locator = args[1];

            verifyStrategy(strategy);

            Method strategyMethod = By.class.getMethod(strategy, String.class);
            BySelector selector = (BySelector) strategyMethod.invoke(By.class, locator);
            UiObject2 seekBar = device.findObject(selector);

            float percentage = Float.parseFloat(args[2]);
            Direction direction = Direction.valueOf(args[3]);

            if (args.length >= 5) {
                speed = Integer.parseInt(args[4]);
            }

            seekBar.swipe(direction, percentage, speed);

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return new Result(true);
    }

    @Override
    public String key() {
        return "uiautomator_seek_bar";
    }
}
