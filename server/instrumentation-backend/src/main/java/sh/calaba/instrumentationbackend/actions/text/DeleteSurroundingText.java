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

    private static String webViewDeleteScript =
            "var activeElement = document.activeElement;\n" +
            "var tagName = activeElement.tagName.toLowerCase();\n" +
            "var from = 0;\n" +
            "var to = 0;\n" +
            "if (tagName === 'input' || tagName === 'textarea') {\n" +
            "    from = activeElement.selectionStart;\n" +
            "    to = activeElement.selectionEnd;\n" +
            "} else if (window.getSelection) {\n" +
            "    var sel = window.getSelection();\n" +
            "    if (sel.rangeCount) {\n" +
            "        var range = sel.getRangeAt(0);\n" +
            "        from = range.startOffset;\n" +
            "        to = range.endOffset;\n" +
            "    }\n" +
            "}\n" +
            "var argBeforeLength = '%1$s';\n" +
            "var argAfterLength = '%2$s';\n" +
            "var beforeLength, afterLength;\n" +
            "var value;\n" +
            "if (tagName === 'input' || tagName === 'textarea') {\n" +
            "    value = activeElement.value;\n" +
            "} else {\n" +
            "    value = activeElement.innerHTML;\n" +
            "}\n" +
            "var textLength = value.length;\n" +
            "if (argBeforeLength < 0) {\n" +
            "    beforeLength = textLength + argBeforeLength + 1;\n" +
            "} else {\n" +
            "    beforeLength = argBeforeLength;\n" +
            "}\n" +
            "if (argAfterLength < 0) {\n" +
            "    afterLength = textLength + argAfterLength + 1;\n" +
            "} else {\n" +
            "    afterLength = argAfterLength;\n" +
            "}\n" +
            "var resultValue = value.substring(0, from - beforeLength) + value.substring(to + afterLength, value.length);\n" +
            "if (tagName === 'input' || tagName === 'textarea') {\n" +
            "    activeElement.value = resultValue;\n" +
            "} else {\n" +
            "    activeElement.innerHTML = resultValue;\n" +
            "}";

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
        int textLength;
        int beforeLength, afterLength;

        if (Build.VERSION.SDK_INT >= 27 && servedView instanceof WebView) {
            WebView webView = (WebView) servedView;

            // Execute JS on the UI thread
            webView.post(new Runnable() {
                @Override
                public void run() {
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setJavaScriptEnabled(true);

                    webView.evaluateJavascript(String.format(webViewDeleteScript, argBeforeLength, argAfterLength), null);
                }
            });

            return Result.successResult();
        }

        if (inputConnection == null) {
            Result.failedResult(getNoFocusedViewMessage());
        }

        // Find length of non-formatted text
        textLength = InfoMethodUtil.getTextLength(inputConnection);

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
