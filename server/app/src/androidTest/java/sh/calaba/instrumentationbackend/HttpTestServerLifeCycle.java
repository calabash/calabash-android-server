package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.content.Intent;

import com.jayway.android.robotium.solo.SoloEnhanced;

import sh.calaba.instrumentationbackend.actions.HttpServer;
import sh.calaba.instrumentationbackend.actions.location.FakeGPSLocation;

public class HttpTestServerLifeCycle implements TestServerLifeCycle {
    private final HttpServer httpServer;
    private ApplicationLifeCycle applicationLifeCycle;

    public HttpTestServerLifeCycle(HttpServer httpServer, ApplicationLifeCycle applicationLifeCycle) {
        this.httpServer = httpServer;
        this.applicationLifeCycle = applicationLifeCycle;
    }

    @Override
    public void start() {
        httpServer.setApplicationStarter(new HttpServer.ApplicationStarter() {
            @Override
            public void startApplication(Intent startIntent) {
                Activity activity =
                        HttpTestServerLifeCycle.this.applicationLifeCycle.start(startIntent);

                InstrumentationBackend.solo =
                        new SoloEnhanced(InstrumentationBackend.instrumentation, activity);
            }
        });

        this.httpServer.setReady();
    }

    public void startAndWaitForKill() {
        start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isHttpServerRunning()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("Thread is interrupted, breaking.");
                        break;
                    }
                }

                stop();
                applicationLifeCycle.stop();
            }
        }).start();
    }

    @Override
    public void stop() {
        httpServer.stop();
        FakeGPSLocation.stopLocationMocking();
    }

    public boolean isHttpServerRunning() {
        return httpServer.isRunning();
    }
}
