package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.content.Intent;

import com.jayway.android.robotium.solo.PublicViewFetcher;
import com.jayway.android.robotium.solo.SoloEnhanced;

import sh.calaba.instrumentationbackend.actions.HttpServer;

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
                InstrumentationBackend.viewFetcher =
                        new PublicViewFetcher(InstrumentationBackend.solo.getActivityUtils());
            }
        });

        this.httpServer.setReady();
    }

    @Override
    public void stop() {
        httpServer.stop();
    }

    public boolean isHttpServerRunning() {
        return httpServer.isRunning();
    }
}
