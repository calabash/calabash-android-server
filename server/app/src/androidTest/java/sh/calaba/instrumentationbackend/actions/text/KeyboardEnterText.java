package sh.calaba.instrumentationbackend.actions.text;

import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

import java.lang.Character;
import java.util.concurrent.Future;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.query.CompletedFuture;

public class KeyboardEnterText extends TextAction {
    private String textToEnter;

    @Override
    protected void parseArguments(String... args) throws IllegalArgumentException {
        if (args.length != 1) {
            throw new IllegalArgumentException("This action takes one argument ([String] text).");
        }

        textToEnter = args[0];
    }

    @Override
    protected String getNoFocusedViewMessage() {
        return "Could not enter text. Make sure that the input element has focus.";
    }

    @Override
    protected Future<Result> executeOnInputThread(final View servedView, final InputConnection inputConnection) {
        if (requiresWebViewInput(servedView)) {
            return evalWebViewInputScript((WebView) servedView, WebViewInputScripts.enterTextScript(textToEnter));
        }

        int start = InfoMethodUtil.getSelectionStart(inputConnection);
        int end = InfoMethodUtil.getSelectionEnd(inputConnection);

        if (Build.VERSION.SDK_INT >= 9) {
            inputConnection.setComposingRegion(start, end);
        }

        for (char c : textToEnter.toCharArray()) {
            inputConnection.commitText(Character.toString(c), 1);
        }

        if (Build.VERSION.SDK_INT >= 9) {
            inputConnection.setComposingRegion(start, end);
        }

        return new CompletedFuture<>(Result.successResult());
    }

    @Override
    public String key() {
        return "keyboard_enter_text";
    }
}
