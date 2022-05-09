package sh.calaba.instrumentationbackend.actions.device;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class UiautomatorExecute implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();
        String text = null;
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
                Method getParentMethod = UiObject2.class.getMethod("getParent");
                UiObject2 parentObject = (UiObject2) getParentMethod.invoke(targetObject);

                Method actionMethod = UiObject2.class.getMethod(extractAction(action));
                result = actionMethod.invoke(parentObject);
            } else {
                Method actionMethod = UiObject2.class.getMethod(action);
                result = actionMethod.invoke(targetObject);
            }

            if (result != null) {
                text = result.toString();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            return new Result(false, e.getMessage());
        } catch (UiObjectNotFoundException e) {
            return new Result(false, e.getMessage());
        }

        return new Result(true, text);
    }

    @Override
    public String key() {
        return "uiautomator_execute";
    }

    private static void verifyStrategy(String strategy) {
        try {
            Strategies.valueOf(strategy);
        } catch (IllegalArgumentException e) {
            List<Strategies> availableStrategies = Arrays.asList(Strategies.values());
            String errorMessage = String.format("Unsupported strategy: %s. The list of available strategies is %s", strategy, availableStrategies);
            throw new IllegalArgumentException(errorMessage);
        }
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
