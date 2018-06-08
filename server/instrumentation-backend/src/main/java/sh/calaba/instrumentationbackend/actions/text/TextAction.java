package sh.calaba.instrumentationbackend.actions.text;

import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;
import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;

public abstract class TextAction implements Action {
    @Override
    public final Result execute(String... args) {
        try {
            parseArguments(args);
        } catch (IllegalArgumentException e) {
            return Result.failedResult(e.getMessage());
        }

        final View servedView;
        final InputConnection inputConnection;

        try {
            servedView = InfoMethodUtil.getServedView();

            // There is a small race condition here, as the input connection may have changed after
            // we have gotten the servedView.
            inputConnection = InfoMethodUtil.getInputConnection();
        } catch (InfoMethodUtil.UnexpectedInputMethodManagerStructureException e) {
            e.printStackTrace();
            return Result.failedResult(e.getMessage());
        }

        if (servedView == null || (inputConnection == null && !requiresWebViewInput(servedView))) {
            return Result.failedResult(getNoFocusedViewMessage());
        }

        FutureTask<Result> futureResult = new FutureTask<Result>(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return executeOnInputThread(servedView, inputConnection);
            }
        });

        UIQueryUtils.postOnViewHandlerOrUiThread(servedView, futureResult);

        try {
            return futureResult.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException executionException) {
            throw new RuntimeException(executionException.getCause());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies is specific WebView input logic required or not.
     * On Android 4 devices we have servedView instanceof WebView and correct input connection, so we don't need to use js logic on old devices.
     * @param view active view
     * @return value indicating whether WebView JS input logic required or not
     */
    public static boolean requiresWebViewInput(View view) {
        return view instanceof WebView && Build.VERSION.SDK_INT > 27;
    }

    /**
     * Executes JS to handle WebView input operations.
     * @param webView active WebView
     * @param script script to execute inside the WebView
     * @return operation result
     */
    public static Result evalWebViewInputScript(WebView webView, String script) {
        try {
            // Execute JS on the UI thread
            webView.post(new Runnable() {
                @Override
                public void run() {
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setJavaScriptEnabled(true);

                    webView.evaluateJavascript(script, null);
                }
            });

            return Result.successResult();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failedResult(e.getMessage());
        }
    }

    protected abstract void parseArguments(String... args) throws IllegalArgumentException;
    protected abstract String getNoFocusedViewMessage();

    /*
        This method is run on the main thread.
     */
    protected abstract Result executeOnInputThread(final View servedView, final InputConnection inputConnection);
}
