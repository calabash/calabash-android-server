package sh.calaba.instrumentationbackend.query.ast.optimization;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;
import sh.calaba.instrumentationbackend.query.ast.UIQueryASTClassName;

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
            // We should not cache UIQueries that use classes that have not been loaded
            // as they might be loaded later on.
            if (value != null) {
                for (UIQueryAST uiQueryAST : value) {
                    if (uiQueryAST instanceof UIQueryASTClassName) {
                        // The query is not for a simple class name, and the qualified class is not
                        // (yet) loaded
                        if (((UIQueryASTClassName) uiQueryAST).qualifiedClassName == null
                                && ((UIQueryASTClassName) uiQueryAST).simpleClassName == null) {
                            if (key == null || key.equals(QueryOptimizationCache.key)) {
                                QueryOptimizationCache.key = null;
                                QueryOptimizationCache.value = null;
                            }

                            return;
                        }
                    }
                }
            }

            QueryOptimizationCache.key = key;
            QueryOptimizationCache.value = value;
        } finally {
            lock.unlock();
        }
    }
}
