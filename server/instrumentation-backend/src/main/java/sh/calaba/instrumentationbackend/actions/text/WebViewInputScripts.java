package sh.calaba.instrumentationbackend.actions.text;

public class WebViewInputScripts {

    // This script allows to add text according to caret position and selection
    static final String InputScript =
            "var activeElement = document.activeElement;\n" +
            "var from = 0;\n" +
            "var to = 0;\n" +
            "var text = '%1$s';\n" +
            "if (text) {\n" +
            "    if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "        from = activeElement.selectionStart;\n" +
            "        to = activeElement.selectionEnd;\n" +
            "        var inputText = activeElement.value;\n" +
            "        activeElement.value = inputText.substring(0, from) + text + inputText.substring(to, inputText.length);\n" +
            "        var caretPos = from + text.length;\n" +
            "        activeElement.focus();\n" +
            "        activeElement.setSelectionRange(caretPos, caretPos);\n" +
            "    } else if (window.getSelection) {\n" +
            "        var sel = window.getSelection();\n" +
            "        if (sel.rangeCount) {\n" +
            "            var range = sel.getRangeAt(0);\n" +
            "            from = range.startOffset;\n" +
            "            to = range.endOffset;\n" +
            "            range.deleteContents();\n" +
            "            var textNode = document.createTextNode(text);\n" +
            "            range.insertNode(textNode);\n" +
            "            range.setStart(textNode, textNode.length);\n" +
            "            range.setEnd(textNode, textNode.length);\n" +
            "            sel.removeAllRanges();\n" +
            "            sel.addRange(range);\n" +
            "        }\n" +
            "    }\n" +
            "}";

    // This script allows to remove text according to caret position and selection
    static final String DeleteScript =
            "var activeElement = document.activeElement;\n" +
            "var from = 0;\n" +
            "var to = 0;\n" +
            "var value;\n" +
            "var argBeforeLength = %1$s;\n" +
            "var argAfterLength = %2$s;\n" +
            "if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    from = activeElement.selectionStart;\n" +
            "    to = activeElement.selectionEnd;\n" +
            "    value = activeElement.value;\n" +
            "} else if (window.getSelection) {\n" +
            "    var sel = window.getSelection();\n" +
            "    if (sel.rangeCount) {\n" +
            "        var range = sel.getRangeAt(0);\n" +
            "        from = range.startOffset;\n" +
            "        to = range.endOffset;\n" +
            "        value = activeElement.innerHTML;\n" +
            "    }\n" +
            "}\n" +
            "var beforeLength, afterLength;\n" +
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
            "if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    activeElement.value = resultValue;\n" +
            "} else {\n" +
            "    activeElement.innerHTML = resultValue;\n" +
            "}";

    // This script allows to add selection inside active element
    static final String SelectScript =
            "var activeElement = document.activeElement;\n" +
            "var tagName = activeElement.tagName.toLowerCase();\n" +
            "var from;\n" +
            "var to;\n" +
            "var textLength;\n" +
            "var argFrom = %1$s;\n" +
            "var argTo = %2$s;\n" +
            "if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    textLength = activeElement.value.length;\n" +
            "} else {\n" +
            "    textLength = activeElement.innerHTML.length;\n" +
            "}\n" +
            "if (argFrom < 0) {\n" +
            "    from = textLength + argFrom + 1;\n" +
            "} else {\n" +
            "    from = argFrom;\n" +
            "}\n" +
            "if (argTo < 0) {\n" +
            "    to = textLength + argTo + 1;\n" +
            "} else {\n" +
            "    to = argTo;\n" +
            "}\n" +
            "if (activeElement.selectionStart !== null && typeof activeElement.selectionStart !== 'undefined') {\n" +
            "    activeElement.focus();\n" +
            "    activeElement.setSelectionRange(from, to);\n" +
            "} else if (window.getSelection) {\n" +
            "    var selection = window.getSelection();\n" +
            "    var range = document.createRange();\n" +
            "    if (from + 1 <= textLength) {\n" +
            "        from++;\n" +
            "    }\n" +
            "    if (to + 1 <= textLength) {\n" +
            "        to++;\n" +
            "    }" +
            "    range.setStart(activeElement.firstChild, from);\n" +
            "    range.setEnd(activeElement.firstChild, to);\n" +
            "    selection.removeAllRanges();\n" +
            "    selection.addRange(range);\n" +
            "}";
}
