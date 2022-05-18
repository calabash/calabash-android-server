package sh.calaba.instrumentationbackend.actions.device;

import java.util.Arrays;
import java.util.List;

public class StrategyVerifier {
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
}
