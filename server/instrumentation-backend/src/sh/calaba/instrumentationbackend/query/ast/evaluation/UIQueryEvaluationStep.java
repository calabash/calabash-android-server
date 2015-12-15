package sh.calaba.instrumentationbackend.query.ast.evaluation;

import android.view.View;

import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;
import sh.calaba.instrumentationbackend.query.ast.UIQueryDirection;
import sh.calaba.instrumentationbackend.query.ast.UIQueryVisibility;

public class UIQueryEvaluationStep implements QueryEvaluationStep {
    private List<?> currentResult;
    private UIQueryEvaluationRules evaluationRules;

    public UIQueryEvaluationStep(List<View> inputViews) {
        currentResult = inputViews;
        evaluationRules = new UIQueryEvaluationRules();
    }

    @Override
    public List<?> step(UIQueryDirection direction) {
        evaluationRules.step(direction);
        
        return currentResult;
    }

    @Override
    public List step(UIQueryVisibility visibility) {
        evaluationRules.step(visibility);
        
        return currentResult;
    }

    @Override
    public List<?> step(UIQueryAST step) {
        evaluationRules.step(step);
        
        currentResult = step.evaluateWithViews(currentResult, evaluationRules.getCurrentDirection(),
                evaluationRules.getCurrentVisibility());

        return currentResult;
    }
}
