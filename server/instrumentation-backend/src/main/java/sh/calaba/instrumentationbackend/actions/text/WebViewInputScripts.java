package sh.calaba.instrumentationbackend.actions.text;

public class WebViewInputScripts {

    private static final String enterScript =
            "function enterCharacter(activeElement, character) {  \n" +
            "  var from = 0; var to = 0;\n" +
            "  if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    from = activeElement.selectionStart;\n" +
            "    to = activeElement.selectionEnd;\n" +
            "    var inputText = activeElement.value;\n" +
            "    activeElement.value = inputText.substring(0, from) + character + inputText.substring(to, inputText.length);\n" +
            "    var caretPos = from + 1;\n" +
            "    activeElement.setSelectionRange(caretPos, caretPos);\n" +
            "  } else if (window.getSelection) {\n" +
            "    var sel = window.getSelection();\n" +
            "    if (sel.rangeCount) {\n" +
            "      var range = sel.getRangeAt(0);\n" +
            "      from = range.startOffset;\n" +
            "      to = range.endOffset;\n" +
            "      range.deleteContents();\n" +
            "      var textNode = document.createTextNode(character);\n" +
            "      range.insertNode(textNode);\n" +
            "      range.setStart(textNode, textNode.length);\n" +
            "      range.setEnd(textNode, textNode.length);\n" +
            "      sel.removeAllRanges();\n" +
            "      sel.addRange(range);\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "function simulateKeyEvent(activeElement, character) {\n" +
            "  var keyboardEvent;\n" +
            "  var charCode = character.charCodeAt(0);\n" +
            "  keyboardEvent = new KeyboardEvent('keydown');\n" +
            "  keyboardEvent.key = character;\n" +
            "  keyboardEvent.charCode = charCode;\n" +
            "  activeElement.dispatchEvent(keyboardEvent);\n" +
            "  keyboardEvent = new KeyboardEvent('keyup');\n" +
            "  keyboardEvent.key = character;\n" +
            "  keyboardEvent.charCode = charCode;\n" +
            "  activeElement.dispatchEvent(keyboardEvent);\n" +
            "  keyboardEvent = new KeyboardEvent('keypress');\n" +
            "  keyboardEvent.key = character;\n" +
            "  keyboardEvent.charCode = charCode;\n" +
            "  activeElement.dispatchEvent(keyboardEvent);\n" +
            "}\n" +
            "function enterText(text) {\n" +
            "  if (text) {\n" +
            "    var element = document.activeElement;\n" +
            "    for (var i = 0; i < text.length; i++) {\n" +
            "      var ch = text.charAt(i);\n" +
            "      enterCharacter(element, ch);\n" +
            "      simulateKeyEvent(element, ch);\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "enterText('%1$s');";

    private static final String deleteScript =
            "function removeCharacter(activeElement, rtl) {  \n" +
            "  var from = 0;\n" +
            "  var value;\n" +
            "  if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    from = activeElement.selectionStart;\n" +
            "    value = activeElement.value;\n" +
            "  } else if (window.getSelection) {\n" +
            "    var sel = window.getSelection();\n" +
            "    if (sel.rangeCount) {\n" +
            "      var range = sel.getRangeAt(0);\n" +
            "      from = range.startOffset;\n" +
            "      value = activeElement.innerHTML;\n" +
            "    }\n" +
            "  }\n" +
            "  var textLength = value.length;\n" +
            "  var resultValue;\n" +
            "  var caretPos;\n" +
            "  if (rtl) {\n" +
            "    caretPos = from - 1;\n" +
            "    resultValue = value.substring(0, caretPos) + value.substring(from, textLength);\n" +
            "  } else {\n" +
            "    caretPos = from;\n" +
            "    resultValue = value.substring(0, from) + value.substring(caretPos + 1, textLength);\n" +
            "  }\n" +
            "  if (caretPos < 0) {\n" +
            "    caretPos = 0;\n" +
            "  }\n" +
            "  if (caretPos > resultValue.length) {\n" +
            "    caretPos = resultValue.length;\n" +
            "  }\n" +
            "  if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    activeElement.value = resultValue;\n" +
            "    activeElement.setSelectionRange(caretPos, caretPos);\n" +
            "  } else {\n" +
            "    activeElement.innerHTML = resultValue;\n" +
            "    if (window.getSelection && activeElement.firstChild) {\n" +
            "      var selection = window.getSelection();\n" +
            "      var newRange = document.createRange();\n" +
            "      newRange.setStart(activeElement.firstChild, caretPos);\n" +
            "      newRange.setEnd(activeElement.firstChild, caretPos);\n" +
            "      selection.removeAllRanges();\n" +
            "      selection.addRange(newRange);\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "function simulateKeyEvent(activeElement, rtl) {\n" +
            "  var keyCode;\n" +
            "  if (rtl) {\n" +
            "    keyCode = 8; // backspace\n" +
            "  } else {\n" +
            "    keyCode = 46; // delete\n" +
            "  }\n" +
            "  var keyboardEvent;\n" +
            "  keyboardEvent = new KeyboardEvent('keydown');\n" +
            "  keyboardEvent.which = keyCode;\n" +
            "  activeElement.dispatchEvent(keyboardEvent);\n" +
            "  keyboardEvent = new KeyboardEvent('keyup');\n" +
            "  keyboardEvent.which = keyCode;\n" +
            "  activeElement.dispatchEvent(keyboardEvent);\n" +
            "  keyboardEvent = new KeyboardEvent('keypress');\n" +
            "  keyboardEvent.which = keyCode;\n" +
            "  activeElement.dispatchEvent(keyboardEvent);\n" +
            "}\n" +
            "function deleteText(argBeforeLength, argAfterLength) {\n" +
            "  var element = document.activeElement;\n" +
            "  var textLength;\n" +
            "  var beforeLength; var afterLength;\n" +
            "  if (element.selectionStart !== null && typeof element.selectionStart !== 'undefined') {\n" +
            "    textLength = element.value.length;\n" +
            "  } else {\n" +
            "    textLength = element.innerHTML.length;\n" +
            "  }\n" +
            "  if (argBeforeLength < 0) {\n" +
            "    beforeLength = textLength + argBeforeLength + 1;\n" +
            "  } else {\n" +
            "    beforeLength = argBeforeLength;\n" +
            "  }\n" +
            "  if (argAfterLength < 0) {\n" +
            "    afterLength = textLength + argAfterLength + 1;\n" +
            "  } else {\n" +
            "    afterLength = argAfterLength;\n" +
            "  }\n" +
            "  for (var i = 0; i < beforeLength; i++) {\n" +
            "    removeCharacter(element, true);\n" +
            "    simulateKeyEvent(element, true);\n" +
            "  }\n" +
            "  for (var j = 0; j < afterLength; j++) {\n" +
            "    removeCharacter(element, false);\n" +
            "    simulateKeyEvent(element, false);\n" +
            "  }\n" +
            "}\n" +
            "deleteText(%1$s,%2$s);";

    private static final String selectScript =
            "function selectText(argFrom, argTo) {\n" +
            "  var activeElement = document.activeElement;\n" +
            "  var tagName = activeElement.tagName.toLowerCase();\n" +
            "  var from;\n" +
            "  var to;\n" +
            "  var textLength;\n" +
            "  if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    textLength = activeElement.value.length;\n" +
            "  } else {\n" +
            "    textLength = activeElement.innerHTML.length;\n" +
            "  }\n" +
            "  if (argFrom < 0) {\n" +
            "    from = textLength + argFrom + 1;\n" +
            "  } else {\n" +
            "    from = argFrom;\n" +
            "  }\n" +
            "  if (argTo < 0) {\n" +
            "    to = textLength + argTo + 1;\n" +
            "  } else {\n" +
            "    to = argTo;\n" +
            "  }\n" +
            "  if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    activeElement.focus();\n" +
            "    activeElement.setSelectionRange(from, to);\n" +
            "  } else if (window.getSelection) {\n" +
            "    var selection = window.getSelection();\n" +
            "    var range = document.createRange();\n" +
            "    range.setStart(activeElement.firstChild, from);\n" +
            "    range.setEnd(activeElement.firstChild, to);\n" +
            "    selection.removeAllRanges();\n" +
            "    selection.addRange(range);\n" +
            "  }\n" +
            "}\n" +
            "selectText(%1$s,%2$s);";

    /**
     * Allows to add text according to caret position and selection
     * @param text text to input
     * @return script to enter text into active WebView field
     */
    public static String enterTextScript(String text) {
        return String.format(enterScript, text.replaceAll("\'", "\\\\x27"));
    }


    /**
     * Allows to delete text according to caret position and selection
     * @param beforeLength number of characters before the current cursor position
     * @param afterLength number of characters after the current cursor position
     * @return script to remove text into active WebView field
     */
    public static String deleteTextScript(int beforeLength, int afterLength) {
        return String.format(deleteScript, beforeLength, afterLength);
    }


    /**
     * Allows to add selection inside active element
     * @param from the character index where the selection should start
     * @param to the character index where the selection should end
     * @return script to select text into active WebView field
     */
    public static String selectTextScript(int from, int to) {
        return String.format(selectScript, from, to);
    }
}
