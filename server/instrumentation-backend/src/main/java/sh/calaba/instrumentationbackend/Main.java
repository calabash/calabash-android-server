package sh.calaba.instrumentationbackend;

import sh.calaba.instrumentationbackend.automation.CalabashAutomationEmbedded;
import sh.calaba.instrumentationbackend.entrypoint.EntryPoint;

public class Main {
    public static void start(EntryPoint entryPoint) {
        CalabashInstrumentation.dontRun = true;
        InstrumentationBackend.setDefaultCalabashAutomation(entryPoint.getCalabashAutomation());
        entryPoint.start();
    }
}
