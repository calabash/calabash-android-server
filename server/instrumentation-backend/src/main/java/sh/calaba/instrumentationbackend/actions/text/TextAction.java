package sh.calaba.instrumentationbackend.actions.text;

import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputConnection;

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

        if (servedView == null) {
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

    protected abstract void parseArguments(String... args) throws IllegalArgumentException;
    protected abstract String getNoFocusedViewMessage();

    /*
        This method is run on the main thread.
     */
    protected abstract Result executeOnInputThread(final View servedView, final InputConnection inputConnection);
}
