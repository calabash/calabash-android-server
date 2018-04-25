package sh.calaba.instrumentationbackend.query.ast.optimization;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.*;
import sh.calaba.instrumentationbackend.query.ast.evaluation.UIQueryEvaluationRules;

public class UIQueryVisibilityOptimizer implements QueryOptimizer {
    @Override
    public List<UIQueryAST> optimize(List<UIQueryAST> queryPath) {
        List<UIQueryAST> optimizedQuery = new ArrayList<UIQueryAST>(queryPath.size());
        UIQueryEvaluationRules evaluationRules = new UIQueryEvaluationRules();

        for (int i = 0; i < queryPath.size(); i++) {
            UIQueryAST step = queryPath.get(i);
            UIQueryAST nextStep;

            if (i+1 < queryPath.size()) {
                nextStep = queryPath.get(i + 1);
            } else {
                nextStep = null;
            }

            if (step instanceof UIQueryDirection) {
                evaluationRules.step((UIQueryDirection) step);
            } else if (step instanceof UIQueryVisibility) {
                evaluationRules.step((UIQueryVisibility) step);
            } else {
                if (nextStep instanceof UIQueryASTPredicate
                        || nextStep instanceof UIQueryASTWith) {
                    // We need to ensure we and no following predicate match for index
                    boolean indexPredicate = false;

                    for (int j = i; j < queryPath.size(); j++) {
                        UIQueryAST s = queryPath.get(j);

                        if (j != i && s instanceof UIQueryASTClassName) {
                            break;
                        } else if (s instanceof UIQueryASTWith) {
                            if (((UIQueryASTWith) s).propertyName.equals("index")) {
                                indexPredicate = true;
                            }
                        } else if (s instanceof UIQueryASTPredicate) {
                            if (((UIQueryASTPredicate) s).propertyName.equals("index")) {
                                indexPredicate = true;
                            }
                        }
                    }

                    if (!indexPredicate) {
                        // If the next query is a predicate, there is no need to filter visibility beforehand

                        // <class> <pred>:<value> becomes <all> <class> <visible> <pred>:<value>

                        optimizedQuery.add(UIQueryVisibility.ALL);
                        optimizedQuery.add(step);
                        optimizedQuery.add(evaluationRules.getCurrentVisibility());
                        optimizedQuery.add(nextStep);
                        i++;
                        continue;
                    }
                }

                evaluationRules.step(step);
            }

            optimizedQuery.add(step);
        }

        return optimizedQuery;
    }
}
