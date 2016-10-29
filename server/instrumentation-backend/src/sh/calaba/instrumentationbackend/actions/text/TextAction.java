package sh.calaba.instrumentationbackend.actions.text;

import android.view.View;
import android.view.inputmethod.InputConnection;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

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
        InputConnection inputConnectionT;
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

        if (servedView == null || inputConnection == null) {
            return Result.failedResult(getNoFocusedViewMessage());
        }

        FutureTask<Result> futureResult = new FutureTask<Result>(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return executeOnUIThread(servedView, inputConnection);
            }
        });

        // HACK: ThreadedInputConnection should only be used while not on the
        // UI-thread.
        if ("org.chromium.content.browser.input.ThreadedInputConnection".equals(inputConnection.getClass().getName())) {
            return executeOnUIThread(servedView, inputConnection);
        } else {
            UIQueryUtils.runOnViewThread(servedView, futureResult);

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
    }

    protected abstract void parseArguments(String... args) throws IllegalArgumentException;
    protected abstract String getNoFocusedViewMessage();

    /*
        This method is run on the main thread.
     */
    protected abstract Result executeOnUIThread(final View servedView, final InputConnection inputConnection);
}
