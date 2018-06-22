package sh.calaba.instrumentationbackend.query.ast.optimization;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.*;
import sh.calaba.instrumentationbackend.query.ast.evaluation.UIQueryEvaluationRules;

public class UIQueryRedundancyOptimizer implements QueryOptimizer {
    @Override
    public List<UIQueryAST> optimize(List<UIQueryAST> queryPath) {
        List<UIQueryAST> optimizedQuery = new ArrayList<UIQueryAST>(queryPath.size());
        UIQueryEvaluationRules evaluationRules = new UIQueryEvaluationRules();

        for (int i = 0; i < queryPath.size(); i++) {
            UIQueryAST step = queryPath.get(i);
            UIQueryAST nextStep;

            if (i + 1 < queryPath.size()) {
                nextStep = queryPath.get(i + 1);
            } else {
                nextStep = null;
            }

            if (step instanceof UIQueryDirection) {
                if (nextStep instanceof UIQueryDirection || nextStep == null) {
                    continue;
                }

                if (step == evaluationRules.getCurrentDirection()) {
                    continue;
                }

                evaluationRules.step((UIQueryDirection) step);
            } else if (step instanceof UIQueryVisibility) {
                if (nextStep instanceof UIQueryVisibility || nextStep == null) {
                    continue;
                }

                if (step == evaluationRules.getCurrentVisibility()) {
                    continue;
                }

                evaluationRules.step((UIQueryVisibility) step);
            } else {
                evaluationRules.step(step);
            }

            optimizedQuery.add(step);
        }

        return optimizedQuery;
    }
}
