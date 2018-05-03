package sh.calaba.instrumentationbackend.query.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface UIObject {
    public Object getObject();
    public<V> Future<V> evaluateAsyncInMainThread(final Callable<V> callable) throws Exception;
}
