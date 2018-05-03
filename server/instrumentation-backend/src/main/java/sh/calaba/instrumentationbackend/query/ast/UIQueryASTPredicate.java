package sh.calaba.instrumentationbackend.query.ast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;

import org.antlr.runtime.tree.CommonTree;

import android.view.View;

import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;
import sh.calaba.instrumentationbackend.query.ui.UIObjectWebResult;

public class UIQueryASTPredicate implements UIQueryAST {
	public final String propertyName;
	public final UIQueryASTPredicateRelation relation;
	public final Object valueToMatch;

	public UIQueryASTPredicate(String text,
			UIQueryASTPredicateRelation parsedRelation, Object parsedValue) {
		this.propertyName = text;
		this.relation = parsedRelation;
		this.valueToMatch = parsedValue;
	}

	public List<UIObject> evaluateWithViews(List<? extends UIObject> inputUIObjects,
													  UIQueryDirection direction,
													  UIQueryVisibility visibility) {
		final List<Future<List<? extends UIObject>>> futureResults;

		try {
			futureResults = new ArrayList<Future<List<? extends UIObject>>>();

			for (UIObject uiObject : UIQueryUtils.uniq(inputUIObjects)) {
				Matcher callable = new Matcher(uiObject);
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
        Matcher(UIObject uiObject) {
			super(uiObject);
        }

		@Override
		protected List<? extends UIObject> matchForUIObject(UIObjectView uiObjectView) {
			View view = evaluateForView(uiObjectView.getObject());

            if (view != null) {
                return Collections.singletonList(new UIObjectView(view));
            } else {
                return Collections.emptyList();
            }
		}

		@Override
		protected List<? extends UIObject> matchForUIObject(UIObjectWebResult uiObjectWebResult) {
            Map<?,?> map = evaluateForMap(uiObjectWebResult.getObject());

            if (map != null) {
                return Collections.singletonList(
                        new UIObjectWebResult(map,
                        uiObjectWebResult.getWebContainer()));
            } else {
                return Collections.emptyList();
            }
		}
	}

	private Map<?,?> evaluateForMap(Map<?,?> map) {
		if (map.containsKey(this.propertyName)) {
			Object value = map.get(this.propertyName);
			if (this.relation.areRelated(value, this.valueToMatch)) {
				return map;
			}
		}

		return null;
	}

	private View evaluateForView(View view) {
		if (this.propertyName.equals("id")) {
			String id = UIQueryUtils.getId(view);

			if (this.relation.areRelated(id, this.valueToMatch)) {
				return view;
			} else {
				// let it fall through and check via general property access
				// in case the user actually wants to compre the real value of
				// getId()
			}
		}

		// there's no tag property for non Views, handle them all here
		if (this.propertyName.equals("tag")) {
			String tag = UIQueryUtils.getTag(view);

			if (this.relation.areRelated(tag, this.valueToMatch)) {
				return view;
			} else {
				return null;
			}
		}

		Method propertyAccessor = UIQueryUtils
				.hasProperty(view, this.propertyName);

		if (propertyAccessor == null) {
			return null;
		}
		Object value = UIQueryUtils.getProperty(view, propertyAccessor);

		if (this.relation.areRelated(value, this.valueToMatch)) {
			return view;
		} else if (this.valueToMatch instanceof String
                    && value != null
                    && this.relation.areRelated(value.toString(), this.valueToMatch)) {
			return view;
		} else {
			return null;
		}
	}

	public static UIQueryASTPredicate newPredicateFromAST(CommonTree step) {
		// TODO Auto-generated method stub
		if (step.getChildCount() != 3) {
			throw new IllegalStateException("Bad Predicate query: "+step+". Expected form {getter RELATION value}.");
		}
		CommonTree prop = (CommonTree) step.getChild(0);
		CommonTree rel = (CommonTree) step.getChild(1);
		CommonTree val = (CommonTree) step.getChild(2);
		return new UIQueryASTPredicate(prop.getText(),
				UIQueryASTPredicate.parseRelation(rel),
				UIQueryUtils.parseValue(val));

	}

	private static UIQueryASTPredicateRelation parseRelation(CommonTree rel) {
		String relText = rel.getText().toUpperCase(Locale.ENGLISH);
		boolean caseSensitive = true;
		final String CASE_INSENSITIVE_SPEC = "[C]";
		if (relText.endsWith(CASE_INSENSITIVE_SPEC)) {
			caseSensitive = false;
			relText = relText.substring(0,relText.length() - CASE_INSENSITIVE_SPEC.length());
		}

		if ("BEGINSWITH".equals(relText)) {
			return new BeginsWithRelation(caseSensitive);
		} else if ("ENDSWITH".equals(relText)) {
			return new EndsWithRelation(caseSensitive);
		} else if ("CONTAINS".equals(relText)) {
			return new ContainsRelation(caseSensitive);
		} else if ("LIKE".equals(relText)) {
			return new LikeRelation(caseSensitive);
		} else if ("<".equals(relText)) {
			return ComparisonOperator.LESSTHAN;
		} else if ("<=".equals(relText)) {
			return ComparisonOperator.LESSTHANOREQUAL;
		} else if ("=".equals(relText)) {
			return ComparisonOperator.EQUAL;
		} else if (">".equals(relText)) {
			return ComparisonOperator.GREATERTHAN;
		} else if (">=".equals(relText)) {
			return ComparisonOperator.GREATERTHANOREQUAL;
		} else if ("!=".equals(relText) || "<>".equals(relText)) {
			return ComparisonOperator.NOTEQUAL;
		} else {
			throw new IllegalStateException("Unsupported Relation: " + relText);
		}
	}

}
