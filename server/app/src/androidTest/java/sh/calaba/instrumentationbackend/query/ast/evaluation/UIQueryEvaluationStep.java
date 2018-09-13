package sh.calaba.instrumentationbackend.query.ast.evaluation;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;
import sh.calaba.instrumentationbackend.query.ast.UIQueryDirection;
import sh.calaba.instrumentationbackend.query.ast.UIQueryVisibility;
import sh.calaba.instrumentationbackend.query.ui.UIObject;

public class UIQueryEvaluationStep implements QueryEvaluationStep<UIObject> {
    private List<UIObject> currentResult;
    private UIQueryEvaluationRules evaluationRules;

    public UIQueryEvaluationStep(List<? extends UIObject> inputUIObjects) {
        currentResult = new ArrayList<UIObject>(inputUIObjects);
        evaluationRules = new UIQueryEvaluationRules();
    }

    @Override
    public List<UIObject> step(UIQueryDirection direction) {
        evaluationRules.step(direction);
        
        return currentResult;
    }

    @Override
    public List<UIObject> step(UIQueryVisibility visibility) {
        evaluationRules.step(visibility);
        
        return currentResult;
    }

    @Override
    public List<UIObject> step(UIQueryAST step) {
        evaluationRules.step(step);
        
        currentResult = step.evaluateWithViews(currentResult, evaluationRules.getCurrentDirection(),
                evaluationRules.getCurrentVisibility());

        return currentResult;
    }
}
