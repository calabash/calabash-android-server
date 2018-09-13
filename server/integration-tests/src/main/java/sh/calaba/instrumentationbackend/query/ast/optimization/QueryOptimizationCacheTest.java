package sh.calaba.instrumentationbackend.query.ast.optimization;

import org.junit.Test;
import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;
import sh.calaba.instrumentationbackend.query.ast.UIQueryASTClassName;
import sh.calaba.instrumentationbackend.query.ast.UIQueryASTWith;
import sh.calaba.instrumentationbackend.query.ast.UIQueryVisibility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class QueryOptimizationCacheTest {
    private UIQueryASTClassName newUIQueryASTClassName(Class<?> clazz, Object name)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<UIQueryASTClassName> constructor = UIQueryASTClassName.class.getDeclaredConstructor(clazz);
        constructor.setAccessible(true);

        return constructor.newInstance(name);
    }

    @Test
    public void shouldNotCacheQueriesWithUnloadedQualifiedClasses() throws Exception {
        List<UIQueryAST> query1 = new ArrayList<UIQueryAST>();
        query1.add(newUIQueryASTClassName(Class.class, null));

        QueryOptimizationCache.cache("query1", query1);
        assertEquals(null, QueryOptimizationCache.getCacheFor("query1"));

        List<UIQueryAST> query2 = new ArrayList<UIQueryAST>();
        query2.add(newUIQueryASTClassName(Class.class, null));
        query2.add(new UIQueryASTWith("foo", new Object()));

        QueryOptimizationCache.cache("query2", query2);
        assertEquals(null, QueryOptimizationCache.getCacheFor("query2"));

        List<UIQueryAST> query3 = new ArrayList<UIQueryAST>();
        query3.add(new UIQueryASTWith("foo", new Object()));
        query3.add(UIQueryVisibility.ALL);
        query3.add(newUIQueryASTClassName(Class.class, null));

        QueryOptimizationCache.cache("query3", query3);
        assertEquals(null, QueryOptimizationCache.getCacheFor("query3"));
    }

    @Test
    public void shouldCacheQueriesWithLoadedQualifiedClasses() throws Exception {
        List<UIQueryAST> query4 = new ArrayList<UIQueryAST>();
        query4.add(newUIQueryASTClassName(Class.class, Class.forName("java.lang.Object")));

        QueryOptimizationCache.cache("query4", query4);
        assertEquals(query4, QueryOptimizationCache.getCacheFor("query4"));
    }

    @Test
    public void shouldCacheQueriesWithSimpleClasses() throws Exception {
        List<UIQueryAST> query5 = new ArrayList<UIQueryAST>();
        query5.add(newUIQueryASTClassName(String.class, "UnLoaded"));

        QueryOptimizationCache.cache("query5", query5);
        assertEquals(query5, QueryOptimizationCache.getCacheFor("query5"));
    }
}
