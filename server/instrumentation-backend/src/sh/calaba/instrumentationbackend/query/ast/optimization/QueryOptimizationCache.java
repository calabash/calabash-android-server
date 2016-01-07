package sh.calaba.instrumentationbackend.query.ast.optimization;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;

public class QueryOptimizationCache {
    private static String key;
    private static List<UIQueryAST> value;
    private static final Lock lock = new ReentrantLock();

    public static List<UIQueryAST> getCacheFor(String key) {
        lock.lock();

        try {
            if (QueryOptimizationCache.key != null && QueryOptimizationCache.key.equals(key)) {
                return value;
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public static void cache(String key, List<UIQueryAST> value) {
        lock.lock();

        try {
            QueryOptimizationCache.key = key;
            QueryOptimizationCache.value = value;
        } finally {
            lock.unlock();
        }
    }
}
