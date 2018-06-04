package sh.calaba.instrumentationbackend.actions.text;

import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.webkit.WebSettings;
import android.webkit.WebView;

import sh.calaba.instrumentationbackend.Result;

public class DeleteSurroundingText extends TextAction {
    private static final String USAGE = "This action takes 2 arguments:\n([int] beforeLength, [int] afterLength)";

    private int argBeforeLength, argAfterLength;

    @Override
    protected void parseArguments(String... args) throws IllegalArgumentException {
        if (args.length != 2) {
            throw new IllegalArgumentException(USAGE);
        }

        try {
            argBeforeLength = Integer.parseInt(args[0]);
            argAfterLength = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(USAGE);
        }
    }

    @Override
    protected String getNoFocusedViewMessage() {
        return "Unable to delete surrounding text, no element has focus";
    }

    @Override
    protected Result executeOnInputThread(final View servedView, final InputConnection inputConnection) {
        int beforeLength, afterLength;

        if (servedView instanceof WebView && Build.VERSION.SDK_INT >= 27) {
            WebView webView = (WebView) servedView;

            // Execute JS on the UI thread
            webView.post(new Runnable() {
                @Override
                public void run() {
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setJavaScriptEnabled(true);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript(String.format(WebViewInputScripts.DeleteScript, argBeforeLength, argAfterLength), null);
                    } else {
                        webView.loadUrl("javascript:" + String.format(WebViewInputScripts.DeleteScript, argBeforeLength, argAfterLength));
                    }
                }
            });

            return Result.successResult();
        }

        if (inputConnection == null) {
            Result.failedResult(getNoFocusedViewMessage());
        }

        // Find length of non-formatted text
        int textLength = InfoMethodUtil.getTextLength(inputConnection);

        if (argBeforeLength < 0) {
            beforeLength = textLength + argBeforeLength + 1;
        } else {
            beforeLength = argBeforeLength;
        }

        if (argAfterLength < 0) {
            afterLength = textLength + argAfterLength + 1;
        } else {
            afterLength = argAfterLength;
        }

        if (Build.VERSION.SDK_INT >= 9) {
            inputConnection.setComposingRegion(InfoMethodUtil.getSelectionStart(inputConnection),
                    InfoMethodUtil.getSelectionEnd(inputConnection));
        }

        inputConnection.deleteSurroundingText(beforeLength, afterLength);

        return Result.successResult();
    }

    @Override
    public String key() {
        return "delete_surrounding_text";
    }
}
