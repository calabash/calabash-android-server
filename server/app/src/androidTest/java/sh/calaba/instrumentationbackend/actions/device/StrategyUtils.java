package sh.calaba.instrumentationbackend.actions.device;

import java.util.Arrays;
import java.util.List;

public class StrategyUtils {
    public static void verifyStrategy(String strategy) {
        try {
            Strategies.valueOf(strategy);
        } catch (IllegalArgumentException e) {
            List<Strategies> availableStrategies = Arrays.asList(Strategies.values());
            String errorMessage =
                  String.format("Unsupported strategy: %s. The list of available strategies is %s", strategy,
                        availableStrategies);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static String convertBySelectorStrategyToUiSelectorStrategy(String bySelectorStrategy) {
        switch (Strategies.valueOf(bySelectorStrategy)) {
            case clazz:
                return "className";
            case res:
                return "resourceId";
            case desc:
                return "description";
            case descContains:
                return "descriptionContains";
            case descEndsWith:
                return "descriptionMatches";
            case descStartWith:
                return "descriptionStartsWith";
            case text:
                return "text";
            case textContains:
                return "textContains";
            case textEndsWith:
                return "textMatches";
            case textStartWith:
                return "textStartsWith";
            case pkg:
                return "packageName";
        }
        return bySelectorStrategy;
    }
}
