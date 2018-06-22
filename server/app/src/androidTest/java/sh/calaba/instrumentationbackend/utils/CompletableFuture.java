package sh.calaba.instrumentationbackend.utils;

import java.util.concurrent.*;

public class CompletableFuture<T> implements Future<T> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile T result = null;

    @Override
    public boolean cancel(boolean b) {
        return false; // indicates that task can not be cancelled
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0L;
    }

    @Override
    public T get() throws InterruptedException {
        latch.await();
        return result;
    }

    @Override
    public T get(long time, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        if (latch.await(time, timeUnit)) {
            return result;
        } else {
            throw new TimeoutException();
        }
    }

    /**
     * Completes the future and wakes up everyone stuck in get().
     * @param result
     * @return
     */
    public CompletableFuture<T> complete(T result) {
        this.result = result;
        latch.countDown();
        return this;
    }
}