package sh.calaba.instrumentationbackend.actions.device;

import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * Created by rajdeepvarma on 10/12/16.
 */
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
        }
        return new Result(latch.getCount() == 0);
    }

    @Override
    public String key() {
        return "wait_for_idle_sync";
    }
}
