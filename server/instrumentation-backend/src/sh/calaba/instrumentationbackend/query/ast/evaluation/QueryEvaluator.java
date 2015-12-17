package sh.calaba.instrumentationbackend.query.ast.evaluation;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.*;

public class QueryEvaluator {
    public static List<?> evaluateQueryForPath(List<UIQueryAST> queryPath,
                                                    QueryEvaluationStep stepHandler) {
        List<?> result = new ArrayList<Object>();

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
