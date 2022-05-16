package sh.calaba.instrumentationbackend.actions.device;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.Until;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

import static sh.calaba.instrumentationbackend.actions.device.StrategyVerifier.verifyStrategy;

public class UiautomatorSetText implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();
        try {
            String strategy = args[0];
            String locator = args[1];
            int index = Integer.parseInt(args[2]);
            String text = args[3];
            boolean executeOnParent = false;
            if (args.length >= 5) {
                executeOnParent = Boolean.parseBoolean(args[4]);
            }

            verifyStrategy(strategy);

            Method strategyMethod = By.class.getMethod(strategy, String.class);
            BySelector selector = (BySelector) strategyMethod.invoke(By.class, locator);

            List<UiObject2> matchingObjects = mDevice.findObjects(selector);
            if (matchingObjects.isEmpty()) {
                String errorMessage = String.format("Found no elements for locator: %s by strategy: %s", locator, strategy);
                throw new UiObjectNotFoundException(errorMessage);
            }
            UiObject2 targetObject = matchingObjects.get(index);

            if (executeOnParent) {
                return setTextOnObject(targetObject.getParent(), text);
            } else {
                return setTextOnObject(targetObject, text);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            return new Result(false, e.getMessage());
        } catch (UiObjectNotFoundException e) {
            return new Result(false, e.getMessage());
        }
    }

    private Result setTextOnObject(UiObject2 targetObject, @NonNull String text)
          throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        targetObject.setText(text);
        targetObject.wait(Until.textMatches(text), 1000);
        if (text.equals(targetObject.getText())) {
            return new Result(true, "Text was set to the view.");
        } else {
            return new Result(false,
                  "It was not possible to set the text to the view, does it have a settable text field?");
        }
    }

    @Override
    public String key() {
        return "uiautomator_set_text";
    }
}
