package sh.calaba.instrumentationbackend.query.ast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


import org.antlr.runtime.tree.CommonTree;

import sh.calaba.instrumentationbackend.actions.webview.CalabashChromeClient;
import sh.calaba.instrumentationbackend.actions.webview.QueryHelper;

import android.os.Build;
import android.view.View;
import android.widget.TextView;

import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.query.ui.UIObjectWebResult;

public class UIQueryASTWith implements UIQueryAST {
	public final String propertyName;
	public final Object value;

	private static final List<String> textMethodNames = Arrays.asList("getText", "getHint");

	private static class TextMethodsCache extends LruCache<Class<?>, List<Method>> {

		TextMethodsCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected List<Method> create(Class<?> key) {
			List<Method> textMethods = new ArrayList<Method>(2);
			for (String methodName : textMethodNames) {
				try {
					textMethods.add(key.getMethod(methodName));
				} catch (NoSuchMethodException e) {
					continue;
				}
			}
			return textMethods;
		}
	}

	private static final TextMethodsCache textMethodsForClass = new TextMethodsCache(1000);

	public UIQueryASTWith(String property, Object value) {
		if (property == null) {
			throw new IllegalArgumentException(
					"Cannot instantiate Filter with null property name");
		}
		this.propertyName = property;
		this.value = value;
	}

	@Override
	public List<UIObject> evaluateWithViews(List<? extends UIObject> inputUIObjects,
											UIQueryDirection direction, UIQueryVisibility visibility) {
		final List<Future<Callable<List<? extends UIObject>>>> futureResults;

        try {
			futureResults = new ArrayList<Future<Callable<List<? extends UIObject>>>>();
			int index = 0;

			for (UIObject uiObject : UIQueryUtils.uniq(inputUIObjects)) {
				Matcher callable = new Matcher(uiObject, index);
				Future<Callable<List<? extends UIObject>>> result = uiObject.evaluateAsyncInMainThread(callable);

				futureResults.add(result);
				index += 1;
			}
		} catch (Exception e) {
			// Thrown from UIQueryUtils.evaluateAsyncInMainThread
			throw new RuntimeException(e);
		}

		final List<UIObject> processedResult;

		try {
            processedResult = new ArrayList<UIObject>();

            for (Future<Callable<List<? extends UIObject>>> future : futureResults) {
                Callable<List<? extends UIObject>> uiObjects = future.get(10, TimeUnit.SECONDS);
                processedResult.addAll(uiObjects.call());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return visibility.evaluateWithViews(processedResult, direction, visibility);
    }

	private class Matcher extends UIQueryMatcher<Callable<List<? extends UIObject>>> {
		private final int index;

		Matcher(UIObject uiObject, int index) {
			super(uiObject);
			this.index = index;
		}

		@Override
		protected Callable<List<? extends UIObject>> matchForUIObject(UIObjectView uiObjectView) {
			if (isDomQuery()) {
				View view = uiObjectView.getObject();

				try {
					return evaluateForWebContainer(new WebContainer(view), null);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				View result = evaluateForView(uiObjectView.getObject(), index);

				if (result != null) {
					return new ResultCallable<List<? extends UIObject>>(
							Collections.singletonList(new UIObjectView(result)));
				}
			}

			return new ResultCallable<List<? extends UIObject>>(new ArrayList<UIObject>());
		}

		@Override
		protected Callable<List<? extends UIObject>> matchForUIObject(UIObjectWebResult uiObjectWebResult) {
			if (isDomQuery()) {
				Map<?,?> map = uiObjectWebResult.getObject();
				Integer index = (Integer) map.get("calSavedIndex");

				try {
					return evaluateForWebContainer(uiObjectWebResult.getWebContainer(), new int[] {index});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				Map<?,?> result = evaluateForMap(uiObjectWebResult.getObject(), index);

				if (result != null) {
					return new ResultCallable<List<? extends UIObject>>(Collections.singletonList(
							new UIObjectWebResult(result, uiObjectWebResult.getWebContainer())));
				}
			}

			return new ResultCallable<List<? extends UIObject>>(new ArrayList<UIObject>());
        }
    }

    private static class ResultCallable<T> implements Callable<T> {
        private T value;

        public ResultCallable(T value) {
            this.value = value;
        }

        @Override
        public T call() throws Exception {
            return value;
        }
    }

    private boolean isDomQuery() {
        return propertyName.equalsIgnoreCase("css") || propertyName.equalsIgnoreCase("xpath");
    }


	private Map<?,?> evaluateForMap(Map<?,?> map, int index) {
        if (this.propertyName.equals("index") && this.value.equals(index)) {
            return map;
        }

		if (map.containsKey(this.propertyName)) {											
			Object value = map.get(this.propertyName);
			if (value == this.value || (value != null && value.equals(this.value))) {
				return map;
			}			
		} 
		return null;
	}

	private View evaluateForView(View view, int index) {
		if (this.propertyName.equals("id") && hasId(view, this.value)) {
			return view;
		} else if (this.propertyName.equals("marked")
				&& isMarked(view, this.value)) {
			return view;
		} else if (this.propertyName.equals("index")
				&& this.value.equals(index)) {
			return view;
		} else {
			Method propertyAccessor = UIQueryUtils.hasProperty(view,
					this.propertyName);
			if (propertyAccessor != null) {
				Object value = UIQueryUtils.getProperty(view, propertyAccessor);

				if (value == this.value
						|| (value != null && value.equals(this.value))) {
					return view;
				} else if (this.value instanceof String
						&& value != null && this.value.equals(value.toString())) {
					return view;
				}
			}
		}

		return null;
	}

	private Callable<List<? extends UIObject>> evaluateForWebContainer(final WebContainer webContainer,
															  final int[] javaScriptElementIds)
			throws Exception {
		if (!(this.value instanceof String)) {
			return null;
		}

		final CalabashChromeClient.WebFuture webFuture = QueryHelper.executeAsyncJavascriptInWebContainer(webContainer,
				"calabash.js", (String) this.value,this.propertyName, javaScriptElementIds);

        return new Callable<List<? extends UIObject>>() {
            @Override
            public List<UIObjectWebResult> call() throws Exception {
                Map<?,?> m = webFuture.get(10, TimeUnit.SECONDS);
                List<UIObjectWebResult> results = new ArrayList<UIObjectWebResult>();

                if (m.containsKey("result")) {
                    UIQueryUtils.MapWebContainer mapper =
                            new UIQueryUtils.MapWebContainer((String) m.get("result"), webContainer);

                    FutureTask<List<Map<String,Object>>> futureTask =
                            new FutureTask<List<Map<String,Object>>>(mapper);
                    UIQueryUtils.runOnViewThread(webContainer.getView(), futureTask);

                    for (Map<String, Object> result : futureTask.get()) {
                        if (result.containsKey("error")) {
                            if (result.containsKey("details")) {
                                throw new InvalidUIQueryException(result.get("error") + ". " + result.get("details"));
                            } else {
                                throw new InvalidUIQueryException(result.get("error").toString());
                            }
                        }

                        results.add(new UIObjectWebResult(result, webContainer));
                    }
                }

                return results;
            }
        };
	}

	private boolean hasId(Object o, Object expectedValue) {
		if (!(o instanceof View)) {
			return false;
		}
		if (!(expectedValue instanceof String)) {
			return false;
		}
		View view = (View) o;
		if (view.getId() == -1)
		{
			return false;
		}
		String expected = (String) expectedValue;
		String id = UIQueryUtils.getId(view);
		return (id != null && id.equals(expected));
	}

	private boolean isMarked(Object o, Object expectedValue) {
		if (!(o instanceof View)) {
			return false;
		}
		if (!(expectedValue instanceof String)) {
			return false;
		}
		View view = (View) o;
		String expected = (String) expectedValue;

		if (hasId(o, expectedValue)) {
			return true;
		}

		CharSequence contentDescription = view.getContentDescription();
		if (contentDescription != null
				&& contentDescription.toString().equals(expected)) {
			return true;
		}

		// Workaround for Android 2.3.x: retrieve text and hint without refection
		// to resolve issue with AppCompat TextView elements, e.g. android.support.v7.widget.AppCompatButton.
		// getDeclaredMethods() for AppCompat TextView elements triggers NoSuchMethodException exception on android API <= 10.
		if (Build.VERSION.SDK_INT <= 10 && view instanceof TextView) {
			TextView element = (TextView) view;
			Object text = element.getText();
			Object hint = element.getHint();

			if ((text != null && text.toString().equals(expected))
					|| (hint != null && hint.toString().equals(expected))) {
				return true;
			}
		}

		List<Method> methods = textMethodsForClass.get(view.getClass());
		for (Method	method : methods) {
			try {
				Object text = method.invoke(view);
				if (text != null && text.toString().equals(expected)) {
					return true;
				}
			} catch (IllegalAccessException e) {
				continue;
			} catch (InvocationTargetException e) {
				continue;
			}
		}
		return false;
	}

	public static UIQueryASTWith fromAST(CommonTree step) {
		CommonTree prop = (CommonTree) step.getChild(0);
		CommonTree val = (CommonTree) step.getChild(1);

		Object parsedVal = UIQueryUtils.parseValue(val);
		return new UIQueryASTWith(prop.getText(), parsedVal);
	}

	@Override
	public String toString() {
		return "With[" + this.propertyName + ":" + this.value + "]";
	}

}
