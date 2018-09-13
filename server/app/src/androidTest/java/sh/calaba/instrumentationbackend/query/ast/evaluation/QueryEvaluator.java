package sh.calaba.instrumentationbackend.query.ast.evaluation;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.*;

public class QueryEvaluator {
    public static<T> List<T> evaluateQueryForPath(List<UIQueryAST> queryPath,
                                                    QueryEvaluationStep<T> stepHandler) {
        List<T> result = new ArrayList<T>();

        for (UIQueryAST step : queryPath) {
            if (step instanceof UIQueryDirection) {
                result = stepHandler.step((UIQueryDirection) step);
            } else if (step instanceof UIQueryVisibility) {
                result = stepHandler.step((UIQueryVisibility) step);
            }  else {
                result = stepHandler.step(step);
            }
        }

        return result;
    }
}
