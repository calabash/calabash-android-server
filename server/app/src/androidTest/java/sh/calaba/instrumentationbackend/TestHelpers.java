package sh.calaba.instrumentationbackend;

public class TestHelpers {
    public static void wait(int durationInSeconds) {
    	wait(new Double(durationInSeconds));
    }


    public static void wait(double durationInSeconds) {
        try {
			Thread.sleep((int)(durationInSeconds * 1000));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

}
