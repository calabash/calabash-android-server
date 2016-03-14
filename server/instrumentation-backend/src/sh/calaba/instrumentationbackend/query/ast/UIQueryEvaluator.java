package sh.calaba.instrumentationbackend.query.ast;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.query.Operation;
import sh.calaba.instrumentationbackend.query.QueryResult;
import sh.calaba.instrumentationbackend.query.UIQueryResultVoid;
import sh.calaba.instrumentationbackend.query.ast.evaluation.QueryEvaluator;
import sh.calaba.instrumentationbackend.query.ast.evaluation.UIQueryEvaluationStep;
import sh.calaba.instrumentationbackend.query.ui.UIObject;

public class UIQueryEvaluator {
	public static QueryResult evaluateQueryWithOptions(List<UIQueryAST> query, List<? extends UIObject> inputViews,
                                                       List<Operation> operations) {
        List<UIObject> uiObjects = evaluateQueryForPath(query, inputViews);
        List<?> result = applyOperations(uiObjects, operations);

        return new QueryResult(result);
	}

	public static List<?> applyOperations(List<? extends UIObject> uiObjects, List<Operation> operations) {
        List<Object> objects = new ArrayList<Object>(uiObjects.size());

        for (UIObject uiObject : uiObjects) {
            objects.add(uiObject.getObject());
        }

		List<?> result = objects;

		for (Operation op : operations) {
			List<Object> nextResult = new ArrayList<Object>(result.size());

			for (Object obj : result) {
				try {
					nextResult.add(op.apply(obj));	
				} catch (Exception e) {
					e.printStackTrace();
					nextResult.add(UIQueryResultVoid.instance.asMap(op.getName(), obj, e.getMessage()));
				}				
			}

			result = nextResult;
		}

		return result;
	}


	private static List<UIObject> evaluateQueryForPath(List<UIQueryAST> queryPath,
			List<? extends UIObject> inputViews) {
		return QueryEvaluator.evaluateQueryForPath(queryPath, new UIQueryEvaluationStep(inputViews));
	}
}
