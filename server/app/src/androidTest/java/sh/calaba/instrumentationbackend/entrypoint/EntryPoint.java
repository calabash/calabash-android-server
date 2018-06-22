package sh.calaba.instrumentationbackend.entrypoint;

import sh.calaba.instrumentationbackend.automation.CalabashAutomation;

public interface EntryPoint {
    public void start();
    public CalabashAutomation getCalabashAutomation();
}
