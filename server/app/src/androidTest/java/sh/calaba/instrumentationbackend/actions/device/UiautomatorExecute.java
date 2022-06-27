package sh.calaba.instrumentationbackend.actions.device;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

import static sh.calaba.instrumentationbackend.actions.device.StrategyUtils.verifyStrategy;

public class UiautomatorExecute implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();
        String resultMessage = null;
        try {
            String strategy = args[0];
            String locator = args[1];
            String index = args[2];
            String action = args[3];

            verifyStrategy(strategy);
            verifyAction(action);

            Method strategyMethod = By.class.getMethod(strategy, String.class);
            BySelector selector = (BySelector) strategyMethod.invoke(By.class, locator);

            List<UiObject2> matchingObjects = mDevice.findObjects(selector);
            if (matchingObjects.isEmpty()) {
                String errorMessage = String.format("Found no elements for locator: %s by strategy: %s", locator, strategy);
                throw new UiObjectNotFoundException(errorMessage);
            }
            UiObject2 targetObject = matchingObjects.get(Integer.parseInt(index));

            Object result;
            if (isActionOnParent(action)) {
                result = executeMethodOnObject(targetObject.getParent(), extractAction(action));
            } else {
                result = executeMethodOnObject(targetObject, action);
            }

            if (result != null) {
                resultMessage = result.toString();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            return new Result(false, e.getMessage());
        } catch (UiObjectNotFoundException e) {
            return new Result(false, e.getMessage());
        }

        return new Result(true, resultMessage);
    }

    private Object executeMethodOnObject(UiObject2 targetObject, String methodName)
          throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method actionMethod = UiObject2.class.getMethod(methodName);
        return actionMethod.invoke(targetObject);
    }

    @Override
    public String key() {
        return "uiautomator_execute";
    }

    private static void verifyAction(String action) {
        try {
            Actions.valueOf(extractAction(action));
        } catch (IllegalArgumentException e) {
            List<String> availableActions = new ArrayList<>();
            for (Actions a : Actions.values()) {
                availableActions.add(a.toString());
                availableActions.add("parent:" + a);
            }
            String errorMessage = String.format("Unsupported action: %s. The list of available actions is %s", action, availableActions);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static boolean isActionOnParent(String action) {
        return action.contains("parent:");
    }

    private static String extractAction(String actionOnParent) {
        return actionOnParent.replace("parent:", "");
    }
}
