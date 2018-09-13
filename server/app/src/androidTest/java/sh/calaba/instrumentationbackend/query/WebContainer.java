package sh.calaba.instrumentationbackend.query;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import sh.calaba.instrumentationbackend.actions.webview.CalabashChromeClient;
import sh.calaba.instrumentationbackend.actions.webview.JavaScriptExecuter;
import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.org.codehaus.jackson.map.ObjectMapper;

public class WebContainer {
    private View view;

    public WebContainer(View webContainer) {
        this.view = webContainer;
    }

    public static boolean isValidWebContainer(View view) {
        WebContainer webContainer = new WebContainer(view);

        if (webContainer.isAndroidWebView()) {
            return true;
        } else if (webContainer.isCrossWalk()) {
            return true;
        } else {
            return false;
        }
    }

    public CalabashChromeClient.WebFuture evaluateAsyncJavaScript(String javaScript) {
        return evaluateAsyncJavaScript(javaScript, false);
    }

    public CalabashChromeClient.WebFuture evaluateAsyncJavaScript(String javaScript, boolean catchJavaScriptExceptions) {
        if (catchJavaScriptExceptions) {
            // Catch all JS exceptions
            javaScript =
                    "(function() {"
                            + "try {"
                            + javaScript
                            + "} catch (exception) {"
                            + "  return \"" + CalabashChromeClient.WebFuture.JS_ERROR_IDENTIFIER + "\" + exception;"
                            + "}"
                            + "})();";
        }

        if (isAndroidWebView()) {
            WebView webView = (WebView) getView();

            if (Build.VERSION.SDK_INT < 19) { // < Android 4.4
                CalabashChromeClient chromeClient = CalabashChromeClient.prepareWebView(webView);
                JavaScriptExecuter javaScriptExecuter = new JavaScriptExecuter(webView);

                // We have to wrap the method call in a function to allow calabash_result = xxx
                javaScript = "calabash_result = " + javaScript + ";prompt('calabash:' + calabash_result);";

                javaScriptExecuter.executeJavaScript(javaScript);

                return chromeClient.getResult();
            } else {
                webView.getSettings().setJavaScriptEnabled(true);

                final CalabashChromeClient.WebFuture webFuture =
                        new CalabashChromeClient.WebFuture(this);

                webView.evaluateJavascript(javaScript, new ValueCallback<String>() {
                    public void onReceiveValue(String response) {
                        ObjectMapper mapper = new ObjectMapper();

                        try {
                            Object value = mapper.readValue(response, Object.class);
                            webFuture.setResult("" + value);
                        } catch (IOException e) {
                            webFuture.completeExceptionally(e);
                        }
                    }
                });

                return webFuture;
            }
        } else if (isCrossWalk()) {
            final CalabashChromeClient.WebFuture webFuture =
                    new CalabashChromeClient.WebFuture(this);

            XWalkContent xWalkContent = XWalkContent.getXWalkContentForView(getView());
            xWalkContent.enableJavaScript();
            xWalkContent.evaluateJavascript(javaScript, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String response) {
                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        Object value = mapper.readValue(response, Object.class);
                        webFuture.setResult("" + value);
                    } catch (IOException e) {
                        webFuture.completeExceptionally(e);
                    }
                }
            });

            return webFuture;
        } else {
            throw new RuntimeException(getView().getClass().getCanonicalName() + " is not recognized a valid web view.");
        }
    }

    public String evaluateSyncJavaScript(final String javaScript) throws ExecutionException {
        return evaluateSyncJavaScript(javaScript, false);
    }

    public String evaluateSyncJavaScript(final String javaScript,
                                         final boolean catchJavaScriptExceptions) throws ExecutionException {
        Callable<CalabashChromeClient.WebFuture> callable =
                new Callable<CalabashChromeClient.WebFuture>() {
            public CalabashChromeClient.WebFuture call() throws Exception {
                return evaluateAsyncJavaScript(javaScript, catchJavaScriptExceptions);
            }
        };

        try {
            Future<CalabashChromeClient.WebFuture> future =
                    new UIObjectView(getView()).evaluateAsyncInMainThread(callable);
            CalabashChromeClient.WebFuture webFuture = future.get(10, TimeUnit.SECONDS);
            Map value = webFuture.get();

            return (String) value.get("result");
        } catch (ExecutionException e) {
            throw e;
        } catch (InterruptedException e) {
            throw new ExecutionException("Timed out waiting for javascript execution", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public float getScale() {
        if (isAndroidWebView()) {
            WebView webView = (WebView) getView();

            return webView.getScale();
        } else if (isCrossWalk()) {
            XWalkContent xWalkContent = XWalkContent.getXWalkContentForView(getView());
            return getView().getContext().getResources().getDisplayMetrics().density * xWalkContent.getScale();
        } else {
            throw new RuntimeException(getView().getClass().getCanonicalName() + " is not recognized a valid web view.");
        }
    }

    public int[] getRenderOffset() {
        // These methods are equivalent.
        // getScroll is the public method in webviews for the same chromium function.

        if (isAndroidWebView()) {
            return new int[] {getView().getScrollX(), getView().getScrollY()};
        } else if (isCrossWalk()) {
            XWalkContent xWalkContent = XWalkContent.getXWalkContentForView(getView());

            return new int[] {xWalkContent.getHorizontalScrollOffset(), xWalkContent.getVerticalScrollOffset()};
        } else {
            throw new RuntimeException(getView().getClass().getCanonicalName() + " is not recognized a valid web view.");
        }
    }


    public Map<String, Integer> translateRectToScreenCoordinates(Map<String, Number> rectangle) {
        try {
            float scale = getScale();
            int[] webviewLocation = UIQueryUtils.getViewLocationOnScreen(getView());
            int[] renderOffset = getRenderOffset();
            int renderOffsetX = renderOffset[0];
            int renderOffsetY = renderOffset[1];

            //center_x, center_y
            //left, top, width, height
            int center_x = (int)translateCoordToScreen(webviewLocation[0] - renderOffsetX, scale,
                    rectangle.get("center_x"));
            int center_y = (int)translateCoordToScreen(webviewLocation[1] - renderOffsetY, scale,
                    rectangle.get("center_y"));

            int x = (int)translateCoordToScreen(webviewLocation[0] - renderOffsetX, scale,
                    rectangle.get("left").doubleValue());
            int y = (int)translateCoordToScreen(webviewLocation[1] - renderOffsetY, scale,
                    rectangle.get("top").doubleValue());

            int width = (int)translateCoordToScreen(0, scale, rectangle.get("width"));
            int height = (int)translateCoordToScreen(0, scale, rectangle.get("height"));

            Map<String,Integer> result = new HashMap<String, Integer>();

            for (String key : rectangle.keySet()) {
                result.put(key, rectangle.get(key).intValue());
            }

            result.put("x", x);
            result.put("y", y);
            result.put("center_x", center_x);
            result.put("center_y", center_y);

            result.put("width", width);
            result.put("height", height);

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public View getView() {
        return view;
    }

    private boolean isAndroidWebView() {
        return (view instanceof android.webkit.WebView);
    }

    private boolean isCrossWalk() {
        return isCrossWalkContentClass(getView().getClass()) ||
                superClassEquals(getView().getClass(), "org.xwalk.core.XWalkView");
    }

    private static boolean isCrossWalkContentClass(Class<?> clz) {
        return superClassEquals(clz, "org.xwalk.core.internal.XWalkContent") ||
                superClassEquals(clz, "org.xwalk.core.internal.XWalkContent$1") ||
                superClassEquals(clz, "org.xwalk.core.internal.XWalkContent$2");
    }

    private static boolean superClassEquals(Class clazz, String className) {
        do {
            if (className.equals(clazz.getName())) {
                return true;
            }
        } while((clazz = clazz.getSuperclass()) != Object.class);

        return false;
    }

    private static View getChildOf(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;

            return viewGroup.getChildAt(0);
        } else {
            return null;
        }
    }

    private float translateCoordToScreen(int offset, float scale, Object point) {
        return offset + ((Number)point).floatValue() *scale;
    }

    private static class XWalkContent {
        private Object xWalkContent;
        
        public static XWalkContent getXWalkContentForView(View view) {
            return new XWalkContent(view);
        }

        private XWalkContent(View view) {
            xWalkContent = view;

            while (!isCrossWalkContentClass(xWalkContent.getClass())) {
                xWalkContent = getChildOf((View)xWalkContent);
            }

            if (superClassEquals(xWalkContent.getClass(), "org.xwalk.core.internal.XWalkContent$1")
                    || superClassEquals(xWalkContent.getClass(), "org.xwalk.core.internal.XWalkContent$2")) {
                try {
                    Field outer = xWalkContent.getClass().getDeclaredField("this$0");
                    outer.setAccessible(true);
                    xWalkContent = outer.get(xWalkContent);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void enableJavaScript() {
            try {
                Method methodGetSettings = xWalkContent.getClass().getMethod("getSettings");
                Object xWalkSettings = methodGetSettings.invoke(xWalkContent);

                Method methodSetJavaScriptEnabled = xWalkSettings.getClass().
                        getMethod("setJavaScriptEnabled", boolean.class);

                methodSetJavaScriptEnabled.invoke(xWalkSettings, true);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public<T> void evaluateJavascript(String javaScript, ValueCallback<T> callback) {
            try {
                Method methodEvaluateJavascript =
                        xWalkContent.getClass().getMethod("evaluateJavascript",
                                String.class, android.webkit.ValueCallback.class);

                methodEvaluateJavascript.invoke(xWalkContent, javaScript, callback);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public float getScale() {
            try {
                Object contentViewCore = getContentViewCore();
                Method getScaleMethod = contentViewCore.getClass().getMethod("getScale");

                return (Float) getScaleMethod.invoke(contentViewCore);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public int getHorizontalScrollOffset() {
            try {
                Object contentViewCore = getContentViewCore();
                Method getScaleMethod = contentViewCore.getClass().getMethod("computeHorizontalScrollOffset");

                return (Integer) getScaleMethod.invoke(contentViewCore);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }



        public int getVerticalScrollOffset() {
            try {
                Object contentViewCore = getContentViewCore();
                Method getScaleMethod = contentViewCore.getClass().getMethod("computeVerticalScrollOffset");

                return (Integer) getScaleMethod.invoke(contentViewCore);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private Object getContentViewCore() {
            try {
                Method getContentViewCoreForTestMethod = xWalkContent.getClass().getMethod("getContentViewCoreForTest");

                return getContentViewCoreForTestMethod.invoke(xWalkContent);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
