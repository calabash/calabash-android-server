package sh.calaba.instrumentationbackend.actions.text;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.softkey.KeyUtil;
import sh.calaba.instrumentationbackend.query.CompletedFuture;

import java.util.concurrent.Future;

public class KeyboardKeyEvent extends TextAction {
    private Integer keyCode;

    @Override
    protected void parseArguments(String... args) throws IllegalArgumentException {
        if (args.length != 1) {
            throw new IllegalArgumentException("This action takes one argument ([String/int] key)");
        }

        String keyString = args[0];

        if (KeyUtil.isNumber(keyString)) {
            keyCode = Integer.parseInt(keyString);
        } else {
            keyCode = KeyUtil.getKey(keyString);
        }

        if (keyCode == null || keyCode < 0) {
            throw new IllegalArgumentException("Could not find key code from argument '" + keyString + "'");
        }
    }

    @Override
    protected String getNoFocusedViewMessage() {
        return "Unable to perform keyboard key event. Make sure that the input element has focus.";
    }

    @Override
    protected Future<Result> executeOnInputThread(final View servedView, final InputConnection inputConnection) {
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));

        return new CompletedFuture<>(Result.successResult());
    }

    @Override
    public String key() {
        return "keyboard_key_event";
    }
}