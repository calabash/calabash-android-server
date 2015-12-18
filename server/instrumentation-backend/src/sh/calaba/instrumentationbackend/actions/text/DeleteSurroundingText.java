package sh.calaba.instrumentationbackend.actions.text;

import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class DeleteSurroundingText implements Action {

    private static final String USAGE = "This action takes 2 arguments:\n([int] beforeLength, [int] afterLength)";

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

        final int argBeforeLength, argAfterLenght;

        try {
            argBeforeLength = Integer.parseInt(args[0]);
            argAfterLenght = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return Result.failedResult(USAGE);
        }

        InstrumentationBackend.solo.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                // Find length of non-formatted text
                int textLength = InfoMethodUtil.getEditableTextLength(editable);
                int beforeLength, afterLength;

                if (argBeforeLength < 0) {
                    beforeLength = textLength + argBeforeLength + 1;
                } else {
                    beforeLength = argBeforeLength;
                }

                if (argAfterLenght < 0) {
                    afterLength = textLength + argAfterLenght + 1;
                } else {
                    afterLength = argAfterLenght;
                }

                if (Build.VERSION.SDK_INT >= 9) {
                    int start = Selection.getSelectionStart(editable);
                    int end = Selection.getSelectionEnd(editable);
                    baseInputConnection.setComposingRegion(start, end);
                }

                baseInputConnection.deleteSurroundingText(beforeLength, afterLength);
            }
        });

        return Result.successResult();
    }

    @Override
    public String key() {
        return "delete_surrounding_text";
    }
}
