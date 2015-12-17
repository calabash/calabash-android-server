package sh.calaba.instrumentationbackend.query.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import sh.calaba.instrumentationbackend.query.Operation;
import sh.calaba.instrumentationbackend.query.QueryResult;
import sh.calaba.instrumentationbackend.query.UIQueryResultVoid;
import sh.calaba.instrumentationbackend.query.ViewMapper;
import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;
import sh.calaba.instrumentationbackend.query.ast.UIQueryDirection;
import sh.calaba.instrumentationbackend.query.ast.UIQueryVisibility;
import sh.calaba.instrumentationbackend.query.ast.evaluation.QueryEvaluator;
import sh.calaba.instrumentationbackend.query.ast.evaluation.UIQueryEvaluationStep;

public class UIQueryEvaluator {
	
	@SuppressWarnings({ "rawtypes" })
	public static QueryResult evaluateQueryWithOptions(List<UIQueryAST> query, List<View> inputViews, List<Operation> operations) {
        List views = evaluateQueryForPath(query, inputViews);
        List result = applyOperations(views, operations);

        // This is a bit of a hack because of the way we pass around values in
        // the result hashmap itself. We will improve if we add a query result type that has
        // metadata in it.
        List modifiedResults = new ArrayList(result.size());

        for (Object object : result) {
            if (object instanceof Map) {
                Map map = new HashMap((Map) object);
                map.remove("calabashWebContainer");
                modifiedResults.add(map);
            } else {
                modifiedResults.add(object);
            }
        }

        return new QueryResult(modifiedResults);
	}



	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List applyOperations(List views, List<Operation> operations) {
		List result = views;
		for(Operation op : operations) {
			List nextResult = new ArrayList(result.size());
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


	private static List evaluateQueryForPath(List<UIQueryAST> queryPath,
			List<View> inputViews) {
		return QueryEvaluator.evaluateQueryForPath(queryPath, new UIQueryEvaluationStep(inputViews));
	}
}
