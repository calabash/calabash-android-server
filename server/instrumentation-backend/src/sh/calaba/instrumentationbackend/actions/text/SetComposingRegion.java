package sh.calaba.instrumentationbackend.actions.text;

import android.os.Build;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputConnection;

import sh.calaba.instrumentationbackend.Result;

public class SetComposingRegion extends TextAction {
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
        return "Unable to set composing region, no element has focus";
    }

    @Override
    protected Result executeOnUIThread(final View servedView, final InputConnection inputConnection) {
        if (Build.VERSION.SDK_INT < 9) {
            return Result.failedResult("Cannot set composing region on Android < 9");
        }

        // Find length of non-formatted text
        int textLength = InfoMethodUtil.getTextLength(servedView, inputConnection);
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

        inputConnection.setComposingRegion(from, to);

        return Result.successResult();
    }

    @Override
    public String key() {
        return "set_composing_region";
    }
}
