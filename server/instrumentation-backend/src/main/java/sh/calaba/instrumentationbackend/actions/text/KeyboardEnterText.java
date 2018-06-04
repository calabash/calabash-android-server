package sh.calaba.instrumentationbackend.actions.text;

import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.Character;

import sh.calaba.instrumentationbackend.Result;

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
        return "Could not enter text. No element has focus.";
    }

    @Override
    protected Result executeOnInputThread(final View servedView, final InputConnection inputConnection) {
        if (Build.VERSION.SDK_INT >= 27 && servedView instanceof WebView) {
            WebView webView = (WebView) servedView;

            // Execute JS on the UI thread
            webView.post(new Runnable() {
                @Override
                public void run() {
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setJavaScriptEnabled(true);

                    webView.evaluateJavascript(String.format(WebViewInputScripts.InputScript, textToEnter), null);
                }
            });

            return Result.successResult();
        }

        if (inputConnection == null) {
            Result.failedResult(getNoFocusedViewMessage());
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

        return Result.successResult();
    }

    @Override
    public String key() {
        return "keyboard_enter_text";
    }
}
