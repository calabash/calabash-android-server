package sh.calaba.instrumentationbackend.actions.text;

import android.text.Selection;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;
import sh.calaba.instrumentationbackend.actions.softkey.KeyUtil;

public class KeyboardKeyEvent implements Action {
    @Override
    public Result execute(String... args) {
        if (args.length != 1) {
            return Result.failedResult("This action takes one argument ([String/int] key).");
        }

        String keyString = args[0];
        final Integer keyCode;

        if (KeyUtil.isNumber(keyString)) {
            keyCode = Integer.parseInt(keyString);
        } else {
            keyCode = KeyUtil.getKey(keyString);
        }

        if (keyCode == null || keyCode < 0) {
            return Result.failedResult("Could not find key code from argument '" + keyString + "'");
        }

        final InputConnection inputConnection = InfoMethodUtil.tryGetInputConnection();

        if (inputConnection == null) {
            return Result.failedResult("Unable to set selection, no element has focus");
        }

        InstrumentationBackend.solo.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            }
        });

        return Result.successResult();
    }

    @Override
    public String key() {
        return "keyboard_key_event";
    }
}