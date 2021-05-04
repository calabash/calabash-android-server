package sh.calaba.instrumentationbackend.actions.device;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class WaitForIdleSync implements Action {
    @Override
    public Result execute(String... args) {
        final CountDownLatch latch = new CountDownLatch(1);
        InstrumentationBackend.instrumentation.waitForIdle(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });
        try {
            latch.await(Integer.parseInt(args[0]), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return new Result(latch.getCount() == 0);
    }

    @Override
    public String key() {
        return "wait_for_idle_sync";
    }
}
