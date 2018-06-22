package sh.calaba.instrumentationbackend.actions.text;

import android.view.View;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.query.CompletedFuture;

import java.util.concurrent.Future;

public class SetSelection extends TextAction {
    private static final String USAGE = "This action takes 2 arguments:\n([int] start, [int] end)";

    private int argFrom, argTo;

    @Override
    protected void parseArguments(String... args) throws IllegalArgumentException {
        if (args.length != 2) {
            throw new IllegalArgumentException(USAGE);
        }

        try {
            argFrom = Integer.parseInt(args[0]);
            argTo = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(USAGE);
        }
    }

    @Override
    protected String getNoFocusedViewMessage() {
        return "Unable to set selection. Make sure that the input element has focus.";
    }

    @Override
    protected Future<Result> executeOnInputThread(final View servedView, final InputConnection inputConnection) {
        if (requiresWebViewInput(servedView)) {
            return evalWebViewInputScript((WebView) servedView, WebViewInputScripts.selectTextScript(argFrom, argTo));
        }

        // Find length of non-formatted text
        int textLength = InfoMethodUtil.getTextLength(inputConnection);
        int from, to;

        if (argFrom < 0) {
            from = textLength + argFrom + 1;
        } else {
            from = argFrom;
        }

        if (argTo < 0) {
            to = textLength + argTo + 1;
        } else {
            to = argTo;
        }

        inputConnection.setSelection(from, to);

        return new CompletedFuture<>(Result.successResult());
    }

    @Override
    public String key() {
        return "set_selection";
    }
}
