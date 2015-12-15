package sh.calaba.instrumentationbackend.query.ast.optimization;

import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;

public class QueryOptimizationCache {
    private static String key;
    private static List<UIQueryAST> value;

    public static synchronized List<UIQueryAST> getCacheFor(String key) {
        if (QueryOptimizationCache.key != null && QueryOptimizationCache.key.equals(key)) {
            return value;
        } else {
            return null;
        }
    }

    public static synchronized void cache(String key, List<UIQueryAST> value) {
        QueryOptimizationCache.key = key;
        QueryOptimizationCache.value = value;
    }
}
