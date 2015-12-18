package sh.calaba.instrumentationbackend.actions.text;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.lang.Character;
import java.lang.reflect.Field;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class KeyboardEnterText implements Action {
    @Override
    public Result execute(String... args) {
        if (args.length != 1) {
            return Result.failedResult("This action takes one argument ([String] text).");
        }

        final InputConnection inputConnection = InfoMethodUtil.tryGetInputConnection();

        if (inputConnection == null) {
            return Result.failedResult("Could not enter text. No element has focus.");
        }

        if (!(inputConnection instanceof BaseInputConnection)) {
            return Result.failedResult("Connection is not an instance of 'BaseInputConnection'");
        }

        final BaseInputConnection baseInputConnection = (BaseInputConnection) inputConnection;


        final Editable editable = baseInputConnection.getEditable();

        if (editable == null) {
            return Result.failedResult("Unable to set selection, not editable");
        }

        final String textToEnter = args[0];
        InstrumentationBackend.solo.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 9) {
                    int start = Selection.getSelectionStart(editable);
                    int end = Selection.getSelectionEnd(editable);
                    baseInputConnection.setComposingRegion(start, end);
                }

                for (char c : textToEnter.toCharArray()) {
                    baseInputConnection.commitText(Character.toString(c), 1);
                }

                if (Build.VERSION.SDK_INT >= 9) {
                    int start = Selection.getSelectionStart(editable);
                    int end = Selection.getSelectionEnd(editable);
                    baseInputConnection.setComposingRegion(start, end);
                }
            }
        });

        return Result.successResult();
    }

    @Override
    public String key() {
        return "keyboard_enter_text";
    }
}
