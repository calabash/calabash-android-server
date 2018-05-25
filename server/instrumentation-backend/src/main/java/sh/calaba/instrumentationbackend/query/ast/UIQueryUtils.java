package sh.calaba.instrumentationbackend.query.ast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import android.app.Activity;

import org.antlr.runtime.tree.CommonTree;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.actions.webview.CalabashChromeClient.WebFuture;
import sh.calaba.instrumentationbackend.actions.webview.QueryHelper;
import sh.calaba.instrumentationbackend.query.Query;
import sh.calaba.instrumentationbackend.query.ViewMapper;
import sh.calaba.instrumentationbackend.query.WebContainer;
import sh.calaba.instrumentationbackend.query.antlr.UIQueryParser;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.utils.ViewWrapper;
import sh.calaba.instrumentationbackend.utils.WindowManagerWrapper;
import sh.calaba.org.codehaus.jackson.JsonProcessingException;
import sh.calaba.org.codehaus.jackson.map.ObjectMapper;
import sh.calaba.org.codehaus.jackson.type.TypeReference;

import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class UIQueryUtils {

	private static final Set<String> DOM_TEXT_TYPES;

	private static final LruCache<Class<?>, Map<String, Method>> properties = new LruCache<Class<?>, Map<String, Method>>(1000);

	private static class ChildMethods {
		final Method childAt;
		final Method childCount;

		private ChildMethods(Method childAt, Method childCount) {
			this.childAt = childAt;
			this.childCount = childCount;
		}
	}

	private static class ChildMethodsCache extends LruCache<Class<?>, ChildMethods> {

		// LruCache does not support null, so use NO_VALUE to handle negative caching
		static ChildMethods NO_VALUE = new ChildMethods(null, null);

		ChildMethodsCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected ChildMethods create(Class<?> key) {
			try {
				Method getChildAt = key.getMethod("getChildAt", int.class);
				getChildAt.setAccessible(true);
				Method getChildCount = key.getMethod("getChildCount");
				getChildCount.setAccessible(true);

				return new ChildMethods(getChildAt, getChildCount);
			} catch (NoSuchMethodException e) {
				return NO_VALUE;
			}
		}
	}

	private static ChildMethodsCache childMethodsCache = new ChildMethodsCache(1000);

	static {
		DOM_TEXT_TYPES = new HashSet<String>();
		DOM_TEXT_TYPES.add("email");
		DOM_TEXT_TYPES.add("text");
		DOM_TEXT_TYPES.add("");
	}

	public static List<View> subviews(Object o) {
		Class<?> clazz = o.getClass();
		ChildMethods childMethods = childMethodsCache.get(clazz);

		if(childMethods == ChildMethodsCache.NO_VALUE) {
			return Collections.emptyList();
		} else {
			try {
				List<View> result = new ArrayList<View>();
				int childCount = (Integer) childMethods.childCount.invoke(o);
				for (int i = 0; i < childCount; i++) {
					Object child = childMethods.childAt.invoke(o, i);

					if (child instanceof View) {
						result.add((View) child);
					}
				}
				return result;
			} catch (IllegalAccessException e) {
				return  Collections.emptyList();
			} catch (InvocationTargetException e) {
				return Collections.emptyList();
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public static Future webContainerSubViews(WebContainer webContainer) {

		Log.i("Calabash", "About to webViewSubViews");


		WebFuture controls = QueryHelper.executeAsyncJavascriptInWebContainer(webContainer,
				"calabash.js", "input,button","css", null);

		return controls;

	}

    public static<T> List<T> uniq(List<T> list) {
        return new ArrayList<T>(new LinkedHashSet<T>(list));
    }

	public static List<View> parents(Object o) {
		try {
			Method getParent = o.getClass().getMethod("getParent");
			getParent.setAccessible(true);

			List<View> result = new ArrayList<View>(8);

			try {
				while (true) {
					Object parent = getParent.invoke(o);

					if (parent == null) {
						return result;
					} else if (parent instanceof View) {
						result.add((View)parent);
					}

					o = parent;
				}
			} catch (IllegalArgumentException e) {
				return result;
			} catch (IllegalAccessException e) {
				return result;
			} catch (InvocationTargetException e) {
				return result;
			}
		} catch (NoSuchMethodException e) {
			return new ArrayList<View>(0);
		}
	}


	@SuppressWarnings({ "rawtypes" })
	public static Method hasProperty(Object o, String propertyName) {
		Class c = o.getClass();

		final Map<String, Method> propForClass = properties.get(c);

		if(propForClass == null) {
			Method method = hasNonCachedProperty(c, propertyName);
			Map firstEntry = new HashMap();
			firstEntry.put(propertyName, method);
			properties.put(c, firstEntry);
			return method;
		} else if (propForClass.containsKey(propertyName)) {
			return propForClass.get(propertyName);
		} else {
			Method method = hasNonCachedProperty(c, propertyName);
			propForClass.put(propertyName, method);
			return method;
		}
	}


	@SuppressWarnings({ "rawtypes" })
	private  static Method hasNonCachedProperty(Class c, String propertyName) {

		Method method = methodOrNull(c, propertyName);
		if (method != null) {
			return method;
		}
		method = methodOrNull(c, "get" + captitalize(propertyName));
		if (method != null) {
			return method;
		}
		method = methodOrNull(c, "is" + captitalize(propertyName));
		return method;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Method methodOrNull(Class c, String methodName) {
		try {
			return c.getMethod(methodName);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	private static String captitalize(String propertyName) {
		return propertyName.substring(0, 1).toUpperCase()
				+ propertyName.substring(1);
	}

	public static Object getProperty(Object receiver, Method m) {
		try {
			return m.invoke(receiver);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

    public static View getRootParent(View view) {
        ViewParent parent = view.getParent();

        if (parent != null && parent instanceof View) {
            return getRootParent((View) parent);
        }

        return view;
    }

    public static Set<View> getRootViews() {
        Activity activity = InstrumentationBackend.getCurrentActivity();

        if (activity == null) {
            System.out.println("Calabash: Activity is null");

            return Collections.emptySet();
        }

        Set<View> parents = new HashSet<View>();

		for (View view : WindowManagerWrapper.fromContext(activity).getViews()) {
			parents.add(getRootParent(view));
		}

        return parents;
    }


    public static boolean isVisible(UIObject uiObject) {
        try {
            return new UIQueryVisibilityMatcher(uiObject).call();
        } catch (Exception e) {
            throw new RuntimeException("Could not detect visibility of " + uiObject, e);
        }
	}

    public static boolean isViewSufficientlyShown(Map<String,Integer> viewRect, Map<String,Integer> parentViewRect) {
        int centerX = viewRect.get("center_x");
        int centerY = viewRect.get("center_y");

        int parentX = parentViewRect.get("x");
        int parentY = parentViewRect.get("y");
        int parentWidth = parentViewRect.get("width");
        int parentHeight = parentViewRect.get("height");
        int windowWidth = parentX + parentWidth;
        int windowHeight = parentY + parentHeight;

        return (windowWidth > centerX && parentX < centerX &&
                windowHeight > centerY && parentY < centerY);
    }

    public static boolean isViewSufficientlyShown(View view) {
        return isViewSufficientlyShown(view, view.getParent());
    }

	public static boolean isClickable(Object v) {
		if (!(v instanceof View)) {
			return true;
		}
		View view = (View) v;

		return view.isClickable();
	}

	public static String getId(View view) {
		return ViewMapper.getIdForView(view);
	}

	public static String getTag(View view) {
		return ViewMapper.getTagForView(view);
	}

	public static<T> Future<T> evaluateAsyncInMainThread(final Callable<T> callable) throws Exception {
		final FutureTask<T> futureTask = new FutureTask<T>(callable);
		InstrumentationBackend.instrumentation.runOnMainSync(futureTask);

		return futureTask;
	}

	public static int[] getViewLocationOnScreen(View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && Build.VERSION.SDK_INT < 28) {
			ViewWrapper viewWrapper = new ViewWrapper(view);

			return viewWrapper.getLocationOnScreen();
		}

		int[] location = new int[2];
		view.getLocationOnScreen(location);

		return location;
	}

	@SuppressWarnings("rawtypes")
	public static Object evaluateSyncInMainThread(Callable callable) {
		try {
			return evaluateAsyncInMainThread(callable)
					.get(10, TimeUnit.SECONDS);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

    public static void runOnViewThread(View view, Runnable runnable) {
        if(view.getHandler() == null || view.getHandler().getLooper() == null || view.getHandler().getLooper().getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            view.post(runnable);
        }
    }

    public static void postOnViewHandlerOrUiThread(View view, Runnable runnable) {
        if (view.getHandler() != null
                && view.getHandler().getLooper() != null) {
            if (view.getHandler().getLooper().getThread() != Thread.currentThread()) {
                view.getHandler().post(runnable);
            } else {
                runnable.run();
            }
        } else {
            view.post(runnable);
        }
    }

    public static Future<List<Map<String, Object>>> mapWebContainerJsonResponseOnViewThread(
            final String jsonResponse, final WebContainer webContainer) {
        FutureTask<List<Map<String, Object>>>  future =
                new FutureTask<List<Map<String, Object>>>(new MapWebContainer(jsonResponse, webContainer));
        runOnViewThread(webContainer.getView(), future);
        return future;
    }

    public static class MapWebContainer implements Callable<List<Map<String, Object>>> {

        private final String jsonResponse;
        private final WebContainer webContainer;

        MapWebContainer(String jsonResponse, WebContainer webContainer) {
            this.jsonResponse = jsonResponse;
            this.webContainer = webContainer;
        }

        @Override
        public List<Map<String, Object>> call() throws Exception {
            List<Map<String, Object>> parsedResult;
            try {
                parsedResult = new ObjectMapper().readValue(jsonResponse,
                        new TypeReference<List<HashMap<String, Object>>>() {
                        });
                for (Map<String, Object> data : parsedResult) {
                    Map<String, Number> rect = (Map<String, Number>) data.get("rect");
                    Map<String, Integer> updatedRect = webContainer.translateRectToScreenCoordinates(rect);
                    data.put("rect", updatedRect);

                    View view = webContainer.getView();
                    String id = ViewMapper.getIdForView(view);

                    data.put("webView", id);
                }

                return parsedResult;
            } catch (JsonProcessingException ignored) {
                System.out.println("Exception in call " + ignored);

                System.out.println("json response: " + jsonResponse);

                try {
                    Map<String, Object> resultAsMap = new ObjectMapper().readValue(
                            jsonResponse, new TypeReference<HashMap>() {
                            });
                    // This usually happens in case of error
                    // check this case
                    System.out.println(resultAsMap);
                    String errorMsg = (String) resultAsMap.get("error");
                    System.out.println(errorMsg);
                    return Collections.singletonList(resultAsMap);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }
        }
    }

	public static Object parseValue(CommonTree val) {
		switch (val.getType()) {
		case UIQueryParser.STRING: {
			String textWithPings = val.getText();
			String text = textWithPings
					.substring(1, textWithPings.length() - 1);
			text = text.replaceAll("\\\\'", "'");
			return text;
		}
		case UIQueryParser.INT:
			return Integer.parseInt(val.getText(), 10);
		case UIQueryParser.BOOL: {
			String text = val.getText();
			return Boolean.parseBoolean(text);
		}
		case UIQueryParser.NIL:
			return null;

		default:
			throw new IllegalArgumentException("Unable to parse value type:"
					+ val.getType() + " text " + val.getText());

		}

	}

	/*
	 *
	 * {"rect"=>{"x"=>0, "y"=>0, "width"=>768, "height"=>1024},
	 * "hit-point"=>{"x"=>384, "y"=>512}, "id"=>"", "action"=>false,
	 * "enabled"=>1, "visible"=>1, "value"=>nil, "type"=>"[object UIAWindow]",
	 * "name"=>nil, "label"=>nil, "children"=> [(samestructure)*]
	 */
	public static Map<?, ?> dump() {
		Query dummyQuery = new Query("not_used");

		List<View> views = new ArrayList<View>();

        for (UIObject uiObject : dummyQuery.rootViews()) {
            if (uiObject.getObject() instanceof View) {
                views.add((View) uiObject.getObject());
            }
        }

		return dumpRecursively(emptyRootView(), views);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<?, ?> mapWithElAsNull(Map<?, ?> dump) {
		if (dump == null)
			return null;
		HashMap result = new HashMap(dump);
		result.put("el", null);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static Map<?, ?> dumpRecursively(Map parentView, List<View> children) {
        ArrayList childrenArray = new ArrayList(32);
        List<Integer> parentPath = Collections.unmodifiableList((List<Integer>) parentView.get("path"));
		for (int i = 0; i < children.size(); i++) {
            View view = children.get(i);
            FutureTask<Map<?, ?>> childrenForChild = new FutureTask<Map<?,?>>(new DumpChild(view, parentPath, i));
            runOnViewThread(view, childrenForChild);

            try {
                childrenArray.add(childrenForChild.get(10, TimeUnit.SECONDS));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

		parentView.put("children", childrenArray);

		return parentView;
	}

    private static class DumpChild implements Callable<Map<?,?>> {

        private final View initialView;
        private final List<Integer> initialParentPath;
        private final int initialIndex;

        public DumpChild(View view, List<Integer> parentPath, int index) {
            this.initialView = view;
            this.initialParentPath = parentPath;
            this.initialIndex = index;
        }

        @Override
        public Map<?,?> call() throws Exception {
            return doDump(initialView, initialParentPath, initialIndex);
        }

        private Map createViewMap(View view, List<Integer> path, int i) {
            Map serializedChild = serializeViewToDump(view);
            List<Integer> childPath = new ArrayList<Integer>(path);
            childPath.add(i);
            serializedChild.put("path", Collections.unmodifiableList(childPath));
            return serializedChild;
        }

        private Map<?,?> doDump(View view, List<Integer> parentPath, int index) {
            Map viewMap = createViewMap(view, parentPath, index);

            if (WebContainer.isValidWebContainer(view)) {
                Future webViewSubViews = webContainerSubViews(new WebContainer(view));
                viewMap.put("children", Collections.singletonList(webViewSubViews));
            }
            else {
                // We are on the owning thread, recur directly
                List<View> childrenList = UIQueryUtils.subviews(view);
                List<Map<?, ?>> children = new ArrayList<Map<?, ?>>(childrenList.size());
                List<Integer> path = Collections.unmodifiableList((List<Integer>) viewMap.get("path"));
                for (int j = 0; j < childrenList.size(); j++) {
                    children.add(doDump(childrenList.get(j), path, j));
                }
                viewMap.put("children", children);
            }
            return viewMap;
        }
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<?, ?> dumpByPath(List<Integer> path) {
		Query dummyQuery = new Query("not_used");

		Map currentView = emptyRootView();
		List<View> currentChildren = new ArrayList<View>();

        for (UIObject uiObject : dummyQuery.rootViews()) {
            if (uiObject.getObject() instanceof View) {
                currentChildren.add((View) uiObject.getObject());
            }
        }

		for (Integer i : path) {
			if (i < currentChildren.size()) {
				View child = currentChildren.get(i);
				currentView = serializeViewToDump(child);
				currentChildren = UIQueryUtils.subviews(child);
			} else {
				return null;
			}

		}

		return currentView;
	}

	/*
 *
                                            "enabled" => true,
                                            "visible" => true,
                                           "children" => [],
                                              "label" => nil,
                                               "rect" => {
                                            "center_y" => 158.5,
                                            "center_x" => 300.0,
                                              "height" => 25,
                                                   "y" => 146,
                                               "width" => 600,
                                                   "x" => 0
                                        },
                                               "type" => "android.widget.TextView",
                                                 "id" => "FacebookTextView",
                                                 "el" => nil,
                                               "name" => "",
                                             "action" => nil,
                                              "value" => "",
                                               "path" => [
                                            [0] 0,
                                            [1] 0,
                                            [2] 2,
                                            [3] 0,
                                            [4] 2
                                        ],
                                          "hit-point" => {
                                            "y" => 158.5,
                                            "x" => 300.0
                                        },
                                        "entry_types" => [
                                            [0] "0"
                                        ]
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<?, ?> serializeViewToDump(Object viewOrMap) {
		if (viewOrMap == null) {
			return null;
		}

		if (viewOrMap instanceof Map)
		{
			Map map = (Map) viewOrMap;
			map.put("el", map);

			Map rect = (Map) map.get("rect");
			Map hitPoint = extractHitPointFromRect(rect);

			map.put("hit-point", hitPoint);
			map.put("enabled", true);
			map.put("visible", true);
			map.put("value", null);
			map.put("type", "dom");
			map.put("name", null);
			map.put("label", null);
			map.put("children", Collections.EMPTY_LIST);
			String html = (String)map.get("html");
			String nodeName = (String) map.get("nodeName");
			if (nodeName != null && nodeName.toLowerCase().equals("input")) {
				String domType = extractDomType(html);
				if (isDomPasswordType(domType)) {
					map.put("entry_types", Collections.singletonList("password"));
				}
				else if (isDomTextType(domType)) {
					map.put("entry_types", Collections.singletonList("text"));
				}
				else {
					map.put("entry_types", Collections.emptyList());
				}
				map.put("value", extractAttribute(html, "value"));
				map.put("type", "dom");
				map.put("name", extractAttribute(html, "name"));
				map.put("label", extractAttribute(html, "title"));
			}

			return map;

		}
		else
		{
			Map m = new HashMap();

			View view = (View) viewOrMap;
			m.put("id", getId(view));
			m.put("el", view);

			Map<String,Integer> rect = ViewMapper.getRectForView(view);
			Map hitPoint = extractHitPointFromRect(rect);

			m.put("rect", rect);
			m.put("hit-point", hitPoint);
			m.put("action", actionForView(view));
			m.put("enabled", view.isEnabled());
			m.put("visible", isVisible(new UIObjectView(view)));
			m.put("entry_types", elementEntryTypes(view));
			m.put("value", extractValueFromView(view));
			m.put("type", ViewMapper.getClassNameForView(view));
			m.put("name", getNameForView(view));
			m.put("label", ViewMapper.getContentDescriptionForView(view));
			return m;
		}




	}

    private static boolean isViewSufficientlyShown(View view, ViewParent viewParent) {
        if (!(viewParent instanceof View)) return true;

        View parent = (View)viewParent;

        if (view.equals(parent) || parent == null) {
            return true;
        }

        Map<String,Integer> viewRect = ViewMapper.getRectForView(view);
        Map<String,Integer> parentRect = ViewMapper.getRectForView(parent);

        return isViewSufficientlyShown(viewRect, parentRect) && isViewSufficientlyShown(view, parent.getParent());
    }

	private static boolean isDomTextType(String domType) {
		if (domType == null) {
			return true;
		}
		return DOM_TEXT_TYPES.contains(domType);
	}

	private static boolean isDomPasswordType(String domType) {
		return "password".equalsIgnoreCase(domType);
	}

	// naive implementation only works for (valid) input tags
	public static String extractDomType(String input) {
		return extractAttribute(input, "type");
	}

	public static String extractAttribute(String input, String attribute) {
		String[] split = input.split(attribute+"=");
		if (split.length == 1) {
			split = input.split(attribute+" =");
		}
		if (split.length > 1) {
			String lastPart = split[1];
			if (lastPart == null) {
				return null;
			}
			if (lastPart.charAt(0) == '"' || lastPart.charAt(0) == '\'') {
				int endIndex = -1;
				for (int i=1;i<lastPart.length();i++) {
					if (lastPart.charAt(i) == '\'' || lastPart.charAt(i) == '"') {
						endIndex = i;
						break;
					}
				}

				if (endIndex > 0) {
					return lastPart.substring(1,endIndex);
				}

			}
		}
		return null;

	}



	public static List<String> elementEntryTypes(View view) {
		if (view instanceof TextView) {
			TextView textView = (TextView) view;
			return mapTextViewInputTypes(textView.getInputType());
		}
		return null;

	}

	public static List<String> mapTextViewInputTypes(int inputType) {
		List<String> inputTypes = new ArrayList<String>();
		if (inputTypeHasTrait(inputType, InputType.TYPE_TEXT_VARIATION_PASSWORD)
				|| inputTypeHasTrait(inputType,
						InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
			inputTypes.add("password");
		}
		if (inputTypeHasTrait(inputType, InputType.TYPE_CLASS_NUMBER)) {
			inputTypes.add("numeric");
		}
		inputTypes.add(String.valueOf(inputType));

		return inputTypes;
	}

	private static boolean inputTypeHasTrait(int inputType, int inputTypeTrait) {
		return (inputType & inputTypeTrait) != 0;
	}

	private static Object getNameForView(View view) {
		Object result = null;
		Method hintMethod = hasProperty(view, "hint");
		if (hintMethod != null) {
			result = getProperty(view, hintMethod);
		}
		if (result != null) {
			return result.toString();
		}
		Method textMethod = hasProperty(view, "text");
		if (textMethod != null) {
			result = getProperty(view, textMethod);
		}
		if (result != null) {
			return result.toString();
		}

		return null;
	}

	public static Object extractValueFromView(View view) {
		if (view instanceof Button) {
			Button b = (Button) view;
			return b.getText().toString();
		} else if (view instanceof CheckBox) {
			CheckBox c = (CheckBox) view;
			return c.isChecked();
		} else if (view instanceof TextView) {
			TextView t = (TextView) view;
			return t.getText().toString();
		}
		return null;
	}

	/*
	 * function action(el) { var normalized = normalize(el); if (!normalized) {
	 * return false; } if (normalized instanceof UIAButton) { return {
	 * "type":'touch', "gesture":'tap' }; } //TODO MORE return false; }
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<?, ?> actionForView(View view) {
		Map result = null;
		if (view instanceof android.widget.Button
				|| view instanceof android.widget.ImageButton) {
			result = new HashMap();
			result.put("type", "touch");
			result.put("gesture", "tap");
		}

		// TODO: obviously many more!
		return result;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map extractHitPointFromRect(Map rect) {
		Map hitPoint = new HashMap();
		hitPoint.put("x", rect.get("center_x"));
		hitPoint.put("y", rect.get("center_y"));
		return hitPoint;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private static Map<?, ?> emptyRootView() {
		return new HashMap() {
			{
				put("id", null);
				put("el", null);
				put("rect", null);
				put("hit-point", null);
				put("action", false);
				put("enabled", false);
				put("visible", true);
				put("value", null);
				put("path", new ArrayList<Integer>());
                    put("type", "[object CalabashRootView]");
				put("name", null);
				put("label", null);
			}
		};
	}

}
