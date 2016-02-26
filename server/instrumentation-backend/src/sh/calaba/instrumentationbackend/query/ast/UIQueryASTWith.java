package sh.calaba.instrumentationbackend.query.ast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


import org.antlr.runtime.tree.CommonTree;

import sh.calaba.instrumentationbackend.actions.webview.CalabashChromeClient;
import sh.calaba.instrumentationbackend.actions.webview.QueryHelper;

import android.view.View;

import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.query.ui.UIObjectWebResult;

public class UIQueryASTWith implements UIQueryAST {
	public final String propertyName;
	public final Object value;

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
		final List<Future<List<? extends UIObject>>> futureResults;

        try {
			futureResults = new ArrayList<Future<List<? extends UIObject>>>();
			int index = 0;

			for (UIObject uiObject : UIQueryUtils.uniq(inputUIObjects)) {
				Matcher callable = new Matcher(uiObject, index);
				Future<List<? extends UIObject>> result = uiObject.evaluateAsyncInMainThread(callable);

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

			for (Future<List<? extends UIObject>> future : futureResults) {
				List<? extends UIObject> uiObjects = future.get(10, TimeUnit.SECONDS);
				processedResult.addAll(uiObjects);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}

		return visibility.evaluateWithViews(processedResult, direction, visibility);
    }

	private class Matcher extends UIQueryMatcher<List<? extends UIObject>> {
		private final int index;

		Matcher(UIObject uiObject, int index) {
			super(uiObject);
			this.index = index;
		}

		@Override
		protected List<? extends UIObject> matchForUIObject(UIObjectView uiObjectView) {
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
					return Collections.singletonList(new UIObjectView(result));
				}
			}

			return null;
		}

		@Override
		protected List<? extends UIObject> matchForUIObject(UIObjectWebResult uiObjectWebResult) {
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
					return Collections.singletonList(
							new UIObjectWebResult(result, uiObjectWebResult.getWebContainer()));
				}
			}

			return null;
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

	private List<UIObjectWebResult> evaluateForWebContainer(WebContainer webContainer,
															  int[] javaScriptElementIds)
			throws Exception {
		if (!(this.value instanceof String)) {
			return null;
		}

		CalabashChromeClient.WebFuture webFuture = QueryHelper.executeAsyncJavascriptInWebContainer(webContainer,
				"calabash.js", (String) this.value,this.propertyName, javaScriptElementIds);

		Map<?,?> m = webFuture.get();
		List<UIObjectWebResult> results = new ArrayList<UIObjectWebResult>();

		if (m.containsKey("result")) {
			UIQueryUtils.MapWebContainer mapper =
					new UIQueryUtils.MapWebContainer((String) m.get("result"), webContainer);

			for (Map<String, Object> result : mapper.call()) {
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

	private boolean hasId(Object o, Object expectedValue) {
		if (!(o instanceof View)) {
			return false;
		}
		if (!(expectedValue instanceof String)) {
			return false;
		}
		View view = (View) o;
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

		ArrayList<String> getTextMethods = new ArrayList<String>();
		getTextMethods.add("getText");
		getTextMethods.add("getHint");

		for (String methodName : getTextMethods) {
			try {
				Method getTextM = view.getClass().getMethod(methodName);
				Object text = getTextM.invoke(view);
				if (text != null && text.toString().equals(expected)) {
					return true;
				}
			} catch (Exception e) {
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
