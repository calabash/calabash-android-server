package sh.calaba.instrumentationbackend.actions.text;

import android.text.Editable;
import android.text.TextUtils;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;
import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class SetSelection implements Action {

    private static final String USAGE = "This action takes 2 arguments:\n([int] start, [int] end)";

    @Override
    public Result execute(final String... args) {
        if (args.length != 2) {
            return Result.failedResult(USAGE);
        } 

        final InputConnection inputConnection = InfoMethodUtil.tryGetInputConnection();

        if (inputConnection == null) {
            return Result.failedResult("Unable to set selection, no element has focus");
        }

        if (!(inputConnection instanceof BaseInputConnection)) {
            return Result.failedResult("Connection is not an instance of 'BaseInputConnection'");
        }

        final BaseInputConnection baseInputConnection = (BaseInputConnection) inputConnection;

        final Editable editable = baseInputConnection.getEditable();

        if (editable == null) {
            return Result.failedResult("Unable to set selection, not editable");
        }

        final int argFrom, argTo;

        try {
            argFrom = Integer.parseInt(args[0]);
            argTo = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return Result.failedResult(USAGE);
        }

        InstrumentationBackend.solo.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                // Find length of non-formatted text
                int textLength = InfoMethodUtil.getEditableTextLength(editable);
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

                baseInputConnection.setSelection(from, to);
            }
        });

        return Result.successResult();
    }

    @Override
    public String key() {
        return "set_selection";
    }
}
