package sh.calaba;

import org.junit.internal.JUnitSystem;
import org.junit.internal.RealSystem;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TestRunner extends JUnitCore {
    private static Class<?>[] testClasses =
            {
                    sh.calaba.json.IntentTest.class
            };

    public static void main(String... args) {
        Result result = start(args);
        System.exit(result.wasSuccessful()?0:1);
    }

    public static Result start(String... args) {
        return new TestRunner().start(new RealSystem(), args);
    }

    public Result start(JUnitSystem system, String[] args) {
        addListener(new RunListener() {
            @Override
            public void testRunStarted(Description description) throws Exception {
                super.testRunStarted(description);
                System.out.println("Starting test run " + description);
                System.out.println("===================================");
                System.out.println("");
            }

            @Override
            public void testRunFinished(Result result) throws Exception {
                super.testRunFinished(result);
                if (result.wasSuccessful()) {
                    System.out.println("\u001B[32mTest run was successful\u001B[0m");
                } else {
                    for (Failure failure : result.getFailures()) {
                        System.out.println("\u001B[31m Failed test: " + failure + "\u001B[0m");

                        failure.getException().printStackTrace();
                    }

                    System.out.println("\u001B[31mTest run failed\u001B[0m");
                }

                System.out.println("");
                System.out.println("Finished test run. Took " + result.getRunTime() + " ms");
                System.out.println("");
            }

            @Override
            public void testStarted(Description description) throws Exception {
                super.testStarted(description);
                System.out.println("Starting test " + description);
            }

            @Override
            public void testFinished(Description description) throws Exception {
                super.testFinished(description);
                System.out.println("Finished test " + description);
                System.out.println("");
            }

            @Override
            public void testFailure(Failure failure) throws Exception {
                super.testFailure(failure);
                System.out.println("\u001B[31mTest failed: " + failure.getMessage() + "\u001B[0m");
            }

            @Override
            public void testAssumptionFailure(Failure failure) {
                super.testAssumptionFailure(failure);
                System.out.println("\u001B[31mTest assumption failure: " + failure.getMessage() + "\u001B[0m");
            }

            @Override
            public void testIgnored(Description description) throws Exception {
                super.testIgnored(description);

                System.out.println("Test ignored " + description);
            }
        });

        Request request = Request.classes(new Computer(), testClasses);

        return this.run(request);
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }
}
