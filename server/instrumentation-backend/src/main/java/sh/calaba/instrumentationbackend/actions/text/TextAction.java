package sh.calaba.instrumentationbackend.actions.text;

import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.concurrent.*;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;
import sh.calaba.instrumentationbackend.query.CompletedFuture;
import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;
import sh.calaba.instrumentationbackend.utils.CompletableFuture;

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

        FutureTask<Future<Result>> futureResult = new FutureTask<>(new Callable<Future<Result>>() {
            @Override
            public Future<Result> call() {
                return executeOnInputThread(servedView, inputConnection);
            }
        });

        UIQueryUtils.postOnViewHandlerOrUiThread(servedView, futureResult);

        try {
            Future<Result> res = futureResult.get(10, TimeUnit.SECONDS);
            return res.get(10, TimeUnit.SECONDS);
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
    public static Future<Result> evalWebViewInputScript(WebView webView, String script) {
        try {
            final CompletableFuture<Result> future = new CompletableFuture<>();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setJavaScriptEnabled(true);

                    webView.evaluateJavascript(script, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String jsError) {
                            // Result from JS is string not a Java null
                            if (jsError.equals("null")) {
                                future.complete(Result.successResult());
                            } else {
                                future.complete(Result.failedResult("JS input injection failed: " + jsError));
                            }
                        }
                    });
                }
            };

            // Execute JS on the UI thread
            webView.post(runnable);

            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return new CompletedFuture<>(Result.failedResult(e.getMessage()));
        }
    }

    protected abstract void parseArguments(String... args) throws IllegalArgumentException;
    protected abstract String getNoFocusedViewMessage();

    /*
        This method is run on the main thread.
     */
    protected abstract Future<Result> executeOnInputThread(final View servedView, final InputConnection inputConnection);
}
