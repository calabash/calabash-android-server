package sh.calaba.instrumentationbackend.actions.text;

import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;

import java.lang.Integer;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Future;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.query.CompletedFuture;

public class PressUserActionButton extends TextAction {
    private static Map<String,Integer> actionCodeMap;

    static {
        actionCodeMap = new HashMap<String, Integer>();
        actionCodeMap.put("normal", EditorInfo.IME_ACTION_UNSPECIFIED);
        actionCodeMap.put("unspecified", EditorInfo.IME_ACTION_UNSPECIFIED);
        actionCodeMap.put("none", EditorInfo.IME_ACTION_NONE);
        actionCodeMap.put("go", EditorInfo.IME_ACTION_GO);
        actionCodeMap.put("search", EditorInfo.IME_ACTION_SEARCH);
        actionCodeMap.put("send", EditorInfo.IME_ACTION_SEND);
        actionCodeMap.put("next", EditorInfo.IME_ACTION_NEXT);
        actionCodeMap.put("done", EditorInfo.IME_ACTION_DONE);
        actionCodeMap.put("previous", EditorInfo.IME_ACTION_PREVIOUS);
    }

    private Integer imeActionCodeArgument;

    @Override
    protected void parseArguments(String... args) throws IllegalArgumentException {
        if (args.length > 1) {
            throw new IllegalArgumentException( "This action takes zero arguments or one argument ([String] action name)");
        }

        if (args.length == 1) {
            imeActionCodeArgument = findActionCode(args[0]);
        }
    }

    @Override
    protected String getNoFocusedViewMessage() {
        return "Could not press user action button. Make sure that the input element has focus.";
    }

    @Override
    protected Future<Result> executeOnInputThread(final View servedView, final InputConnection inputConnection) {
        final Integer imeActionCode;

        if (imeActionCodeArgument != null) {
            imeActionCode = imeActionCodeArgument;
        } else {
            imeActionCode = findActionCode(servedView);
        }

        if (imeActionCode == null) {
            KeyboardEnterText keyboardEnterTextAction = new KeyboardEnterText();
            keyboardEnterTextAction.parseArguments("\n");

            return keyboardEnterTextAction.executeOnInputThread(servedView, inputConnection);
        } else {
            inputConnection.performEditorAction(imeActionCode);

            return new CompletedFuture<>(Result.successResult());
        }
    }

    @Override
    public String key() {
        return "press_user_action_button";
    }

    private Integer findActionCode(View view) {
        EditorInfo editorInfo = new EditorInfo();
        view.onCreateInputConnection(editorInfo);

        int actionId = editorInfo.actionId;
        int imeOptions = editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            int inputType = textView.getInputType();
            int inputTypeFlags = inputType & InputType.TYPE_MASK_FLAGS;

            if ((inputTypeFlags & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_MULTI_LINE) {
                return null;
            }
        }

        if (actionId > 0) {
            return actionId;
        } else if (imeOptions > 0) {
            return imeOptions;
        } else {
            return findActionCode("done");
        }
    }

    private int findActionCode(String actionName) throws IllegalArgumentException {
        actionName = actionName.toLowerCase();
        Integer actionCode;

        try {
            actionCode = actionCodeMap.get(actionName);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Action name '" + actionName + "' invalid");
        }

        if (actionCode == null) {
            throw new IllegalArgumentException("Action name '" + actionName + "' invalid");
        } else {
            return actionCode;
        }
    }
}