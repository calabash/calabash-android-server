package sh.calaba.instrumentationbackend.query.ast.optimization;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import sh.calaba.instrumentationbackend.utils.SlidingMap;

public class UIQueryASTClassNameCache {
    private static Map<String, Class<?>> map = new SlidingMap<String, Class<?>>(20);
    private static Lock lock = new ReentrantLock();

    public static Class<?> loadedClass(String className) {
        lock.lock();

        try {
            return map.get(className);
        } finally {
            lock.unlock();
        }
    }

    public static void markAsLoaded(String className, Class<?> clazz) {
        lock.lock();

        try {
            map.put(className, clazz);
        } finally {
            lock.unlock();
        }
    }
}
