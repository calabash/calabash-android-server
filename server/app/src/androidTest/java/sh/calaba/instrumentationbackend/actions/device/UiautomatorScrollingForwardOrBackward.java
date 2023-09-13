package sh.calaba.instrumentationbackend.actions.device;

import androidx.test.uiautomator.UiObjectNotFoundException;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;

import java.lang.reflect.InvocationTargetException;

import static sh.calaba.instrumentationbackend.actions.device.ScrollToElementActionHelper.scrollToTargetByDirection;


public class UiautomatorScrollingForwardOrBackward {
    private String operationKey;
    private Boolean isHorizontal;

    public UiautomatorScrollingForwardOrBackward(String operationKey, Boolean isHorizontal) {
        this.operationKey = operationKey;
        this.isHorizontal = isHorizontal;
    }

    public Result execute(String... args) {
        InstrumentationBackend.getUiDevice();
        try {
            String direction = args[0];
            String targetBySelectorStrategy = args[1];
            String targetLocator = args[2];

            int maxScrolls = 10;
            if (args.length >= 3) {
                maxScrolls = Integer.parseInt(args[3]);
            }

            ScrollDirection scrollDirection = ScrollDirection.valueOf(direction);

            scrollToTargetByDirection(targetBySelectorStrategy, targetLocator, scrollDirection, maxScrolls, isHorizontal);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (UiObjectNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return new Result(true);
    }

    public String key() {
        return operationKey;
    }
}
