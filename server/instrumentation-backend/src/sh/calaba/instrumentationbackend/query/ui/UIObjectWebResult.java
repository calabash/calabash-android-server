package sh.calaba.instrumentationbackend.query.ui;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;

public class UIObjectWebResult implements UIObject {
    private Map<?,?> map;
    private final WebContainer webContainer;

    public UIObjectWebResult(Map<?,?> map, WebContainer webContainer) {
        this.map = map;
        this.webContainer = webContainer;
    }

    @Override
    public Map<?,?> getObject() {
        return map;
    }

    public WebContainer getWebContainer() {
        return webContainer;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String,Integer> getRect() {
        return (Map) map.get("rect");
    }

    @Override
    public <V> Future<V> evaluateAsyncInMainThread(Callable<V> callable) throws Exception {
        return UIQueryUtils.evaluateAsyncInMainThread(callable);
    }

    public class Rect {
        public int x, y, width, height, centerX, centerY;

        public Rect(int x, int y, int width, int height, int centerX, int centerY) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.centerX = centerX;
            this.centerY = centerY;
        }
    }
}
