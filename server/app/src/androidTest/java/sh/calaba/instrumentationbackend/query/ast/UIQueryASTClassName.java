package sh.calaba.instrumentationbackend.query.ast;

import android.content.Context;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.query.ast.optimization.UIQueryASTClassNameCache;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.query.ui.UIObjectWebResult;

public class UIQueryASTClassName implements UIQueryAST {
	public final String simpleClassName;	
	public final Class<?> qualifiedClassName;

	/*
		Creates a new instance of UIQueryASTClassName by the given qualified class name.
		If the class has not been loaded, the qualifiedClass set will be null
	 */
	public static UIQueryASTClassName fromQualifiedClassName(String qualifiedClassName) {
		ClassLoader classLoader;
		Context context = InstrumentationBackend.instrumentation.getTargetContext();

		if (context == null) {
			System.out.println("targetContext is null, loading this ClassLoader");
			classLoader = UIQueryASTClassName.class.getClassLoader();
		} else {
			classLoader = context.getClassLoader();
		}

		if (classLoader == null) {
			return new UIQueryASTClassName((Class<?>)null);
		}

		return new UIQueryASTClassName(findLoadedClass(classLoader, qualifiedClassName));
	}

	public static UIQueryASTClassName fromSimpleClassName(String simpleClassName) {
		return new UIQueryASTClassName(simpleClassName);
	}

	public static UIQueryASTClassName fromClass(Class<?> clz) {
		return new UIQueryASTClassName(clz);
	}

	private static Class<?> findLoadedClass(ClassLoader classLoader, String qualifiedClassName) {
		Class<?> classFromCache = UIQueryASTClassNameCache.loadedClass(qualifiedClassName);

		if (classFromCache != null) {
			return classFromCache;
		}

		try {
			Method findLoadedClassMethod =
                    ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
			findLoadedClassMethod.setAccessible(true);

			Class<?> foundClass = (Class<?>) findLoadedClassMethod.invoke(classLoader, qualifiedClassName);

			if (foundClass == null) {
				if (classLoader.getParent() != null) {
					return findLoadedClass(classLoader.getParent(), qualifiedClassName);
				}
			}

			if (foundClass != null) {
				UIQueryASTClassNameCache.markAsLoaded(qualifiedClassName, foundClass);
			}

			return foundClass;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private UIQueryASTClassName(String simpleClassName) {
		this.simpleClassName = simpleClassName;
		this.qualifiedClassName = null;
	}
	
	private UIQueryASTClassName(Class<?> qualifiedClassName) {
		this.qualifiedClassName = qualifiedClassName;
		this.simpleClassName = null;		
	}

	@Override
	public List<UIObject> evaluateWithViews(List<? extends UIObject> inputUIObjects,
														 UIQueryDirection direction,
														 UIQueryVisibility visibility) {
        final List<Future<List<? extends UIObject>>> futureResults;

        try {
            futureResults = new ArrayList<Future<List<? extends UIObject>>>();

            for (UIObject uiObject : UIQueryUtils.uniq(inputUIObjects)) {
                Matcher callable = new Matcher(uiObject, direction);
                Future<List<? extends UIObject>> result = uiObject.evaluateAsyncInMainThread(callable);

                futureResults.add(result);
            }
        } catch (Exception e) {
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
        private final UIQueryDirection direction;

        Matcher(UIObject uiObject, final UIQueryDirection direction) {
            super(uiObject);
            this.direction = direction;
        }

        @Override
        protected List<? extends UIObject> matchForUIObject(UIObjectView uiObjectView) {
            switch(direction) {
                case DESCENDANT:
                    return descendantMatches(uiObjectView);
                case CHILD:
                    return childMatches(uiObjectView);
                case PARENT:
                    return parentMatches(uiObjectView);
                case SIBLING:
                    return siblingMatches(uiObjectView);
                default:
                    throw new InvalidUIQueryException("Invalid direction '" + direction + "'");
            }
        }

        @Override
        protected List<? extends UIObject> matchForUIObject(UIObjectWebResult uiObjectWebResult) {
            throw new InvalidUIQueryException("Cannot query by class name for web-results");
        }
    }

	protected List<UIObjectView> siblingMatches(UIObjectView uiObjectView) {
        List<UIObjectView> result = new ArrayList<UIObjectView>();
		List<View> parents = UIQueryUtils.parents(uiObjectView.getObject());

		if (parents != null && !parents.isEmpty()) {
			View immediateParent = parents.get(0);

			for (View v : UIQueryUtils.subviews(immediateParent)) {
				if (v != uiObjectView.getObject() && match(v)) {
					result.add(new UIObjectView(v));
				}
			}									
		}

        return result;
	}

	private List<UIObjectView> parentMatches(UIObjectView uiObjectView) {
        List<UIObjectView> result = new ArrayList<UIObjectView>();

		for (View parent : UIQueryUtils.parents(uiObjectView.getObject())) {
			if (match(parent)) {
				result.add(new UIObjectView(parent));
			}
		}

        return result;
	}

	private List<UIObjectView> childMatches(UIObjectView uiObjectView) {
        List<UIObjectView> result = new ArrayList<UIObjectView>();

		for (View child : UIQueryUtils.subviews(uiObjectView.getObject())) {
			if (match(child)) {
				result.add(new UIObjectView(child));
			}
		}

        return result;
	}

	private List<UIObjectView> descendantMatches(UIObjectView uiObjectView) {
        List<UIObjectView> result = new ArrayList<UIObjectView>();

		if (match(uiObjectView.getObject())) {
			result.add(uiObjectView);
		}
				
		for (View child : UIQueryUtils.subviews(uiObjectView.getObject())) {
			result.addAll(descendantMatches(new UIObjectView(child)));
		}

        return result;
	}
	
	private boolean match(Object o) {
		if (this.simpleClassName == null && this.qualifiedClassName == null) {
			return false;
		}
		return matchSimpleClassName(o,this.simpleClassName) ||
				matchQualifiedClassName(o,this.qualifiedClassName);
	}

	public static boolean matchQualifiedClassName(Object o, Class<?> qualifiedClassName) {
		return qualifiedClassName != null && qualifiedClassName.isAssignableFrom(o.getClass());
	}

	public static boolean matchSimpleClassName(Object o, String simpleClassName) {
		return simpleClassName != null && simpleClassName.equalsIgnoreCase(o.getClass().getSimpleName());
	}
	
	public String toString() {
		if (this.simpleClassName == null && this.qualifiedClassName == null) {
			return "Class[null]";	
		}

		if (this.simpleClassName != null) 
		{
			return "Class["+this.simpleClassName+"]";	
		}
		else 
		{
			return "Class["+this.qualifiedClassName+"]";
		}
		
	}
}
