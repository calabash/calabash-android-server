(function() {
    /** David Mark's isHostMethod function,
     * http://peter.michaux.ca/articles/feature-detection-state-of-the-art-browser-scripting
     * Modified to use strict equality
     */
    function isHostMethod (object, property)
    {
        var t = typeof object[property];
        return t==='function' ||
            (!!(t==='object' && object[property])) ||
            t==='unknown';
    }
    /*http://www.w3.org/TR/DOM-Level-2-Core/core.html*/
    var NODE_TYPES = {
        /*ELEMENT_NODE                   : */ 1 : 'ELEMENT_NODE',
        /*ATTRIBUTE_NODE                 : */ 2: 'ATTRIBUTE_NODE',
        /*TEXT_NODE                      : */ 3 : 'TEXT_NODE',
        /*DOCUMENT_NODE                  : */ 9 : 'DOCUMENT_NODE'
    };

    function computeRectForNode(object, contentWindow, parent)
    {
        var res = {}, boundingBox;
        if (isHostMethod(object,'getBoundingClientRect'))
        {
            boundingBox = object.getBoundingClientRect();
            var windowLeft, windowTop;

            if (parent != null && contentWindow !== window) {
                var parentRect = parent.getBoundingClientRect();
                windowLeft = parentRect.left;
                windowTop = parentRect.top;
            } else {
                windowLeft = 0;
                windowTop = 0;
            }
            var rect = {};
            rect.width = boundingBox.width;
            rect.height = boundingBox.height;
            rect.left = boundingBox.left + windowLeft;
            rect.top = boundingBox.top + windowTop;
            res.rect = rect;
            res.rect.center_x = rect.left + Math.floor(rect.width/2);
            res.rect.center_y = rect.top + Math.floor(rect.height/2);
        }
        res.nodeType = NODE_TYPES[object.nodeType] || res.nodeType + ' (Unexpected)';
        res.nodeName = object.nodeName;
        res.id = object.id || '';
        res['class'] = object.className || '';
        if (object.href)
        {
            res.href = object.href;
        }
        if (object.hasOwnProperty('value'))
        {
            res.value = object.value || '';
        }
        res.html = object.outerHTML || '';
        res.textContent = object.textContent;
        return res;
    }

    function toJSON(object, contentWindow, parent) {
        var res, i, N, spanEl, parentEl;
        log("Im going to toJSON " + object);

        if (typeof object === 'undefined') {
            throw {message: 'Calling toJSON with undefined'};
        }
        else if (object instanceof contentWindow.Text) {
            parentEl = object.parentElement;
            if (parentEl) {
                spanEl = document.createElement("calabash");
                spanEl.style.display = "inline";
                spanEl.innerHTML = object.textContent;
                parentEl.replaceChild(spanEl, object);
                res = computeRectForNode(spanEl, contentWindow, parent);
                res.nodeType = NODE_TYPES[object.nodeType];
                delete res.nodeName;
                delete res.id;
                delete res['class'];

                parentEl.replaceChild(object, spanEl);
            }
            else {
                res = object;
            }


        }
        else if (object instanceof contentWindow.Node) {
            log("Here");
            res = computeRectForNode(object, contentWindow, parent);
        }
        else if (object instanceof contentWindow.NodeList ||
            (typeof object == 'object' && object &&
            typeof object.length === 'number' &&
            object.length > 0
            && typeof object[0] !== 'undefined')) {
            res = [];
            for (i = 0, N = object.length; i < N; i++) {
                res[i] = toJSON(object[i], contentWindow, parent);
            }
        }
        else {
            log("Here2");
            res = object;
        }

        if (!Array.isArray(res)) {
            log("Res is not an array");
            log("Old res: " + res);
            res['calSavedIndex'] = addSavedResults(object);
            log("New res: " + res);
        } else {
            log("res is an array");
        }

        return res;
    }

    /* TODO: Fix this, it is broken as `object` is now {object: object, window: x} */
    function applyMethods(object, arguments) {
        var length = arguments.length;

        for(var i = 0; i < length; i++) {
            var argument = arguments[i];

            if (typeof argument === 'string') {
                argument = {method_name: argument, arguments: []}
            }

            var methodName = argument.method_name;
            var methodArguments = argument.arguments;

            if (typeof object[methodName] === 'undefined') {
                var type = Object.prototype.toString.call(object);

                object =
                {
                    error: "No such method '" + methodName + "'",
                    methodName: methodName,
                    receiverString: object.constructor.name,
                    receiverClass: type
                };

                break;
            } else {
                object = object[methodName].apply(object, methodArguments);
            }
        }
    }

    function getSavedResult(index) {
        return window['calSavedResults'] && window['calSavedResults'][index];
    }

    /* Adds a saved result and returns the index of it*/
    function resetSavedResults() {
        return window['calSavedResults'] = [];
    }

    /* Adds a saved result and returns the index of it*/
    function addSavedResults(value) {
        return window['calSavedResults'].push(value) - 1;
    }

    var exp = '%@' /* dynamic */,
        queryType = '%@' /* dynamic */,
        arguments = '%@' /* dynamic */,
        inElement = '%@' /* dynamic, set as an array of calSavedResult indexes */;

    /* Returns results from a query specified by `exp` with an object of the HTML elements and window*/
    function find(containerDocument, parentContainer) {
        var res = [];
        var nodes = null;
        var i, N;

        if (typeof containerDocument === 'undefined') {
            throw new Error("Container has no document attached");
        }

        var containerWindow = containerDocument.defaultView || containerDocument.ownerDocument.defaultView;

        if (typeof containerDocument.defaultView === 'undefined') {
            throw new Error("Container '" + containerDocument + "' has no defaultView");
        }

        log("Finding " + exp);

        if (queryType==='xpath') {
            nodes = containerDocument.evaluate(exp, containerDocument, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
            for (i=0,N=nodes.snapshotLength;i<N;i++) {
                res[i] = nodes.snapshotItem(i);
            }
        } else {
            res = containerDocument.querySelectorAll(exp);
        }

        log("I found");

        for (var i = 0; i < res.length; i++) {
            log(res[i]);
        }

        return {object: res, window: containerWindow, parent: parentContainer};
    }

    function log(message) {
        //document.getElementById('input').innerHTML += message + "\n";
    }

    try {
        var res = [];

        if (inElement === '%@') {
            res = res.concat(find(window.document, null));
        } else {
            if (!Array.isArray(inElement)) {
                inElement = [inElement];
            }

            for (var i = 0; i < inElement.length; i++) {
                var element = getSavedResult(inElement[i]);

                if (typeof element !== 'undefined') {
                    if (typeof element.contentWindow !== 'undefined') {
                        log("Finding in " + element);
                        res = res.concat(find(element.contentWindow.document, element));
                    } else {
                        res = res.concat(find(element, element));
                    }
                }
            }
        }
    } catch (e) {
        return JSON.stringify({error: 'Exception while running query: ' + exp, details: e.toString()});
    } finally {
        resetSavedResults();
    }

    if (arguments !== '%@') {
        var length = res.length;

        for (var i = 0; i < length; i++) {
            res[i] = applyMethods(res[i], arguments);
        }
    }

    var json;

    if (Array.isArray(res)) {
        log("res is array");
        json = [];

        for (var i = 0; i < res.length; i++) {
            json = json.concat(toJSON(res[i].object, res[i].window, res[i].parent));
        }
    } else {
        json = toJSON(res.object, res.window, res.parent);
    }

    return JSON.stringify(json);
})();