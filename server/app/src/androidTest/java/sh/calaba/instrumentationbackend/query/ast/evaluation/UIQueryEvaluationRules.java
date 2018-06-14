package sh.calaba.instrumentationbackend.query.ast.evaluation;

import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;
import sh.calaba.instrumentationbackend.query.ast.UIQueryDirection;
import sh.calaba.instrumentationbackend.query.ast.UIQueryVisibility;

public class UIQueryEvaluationRules implements QueryEvaluationStep {
    private UIQueryDirection currentDirection;
    private UIQueryVisibility currentVisibility;

    public UIQueryEvaluationRules() {
        currentDirection = UIQueryDirection.DESCENDANT;
        currentVisibility = UIQueryVisibility.VISIBLE;
    }

    @Override
    public List<?> step(UIQueryDirection direction) {
        currentDirection = direction;

        return null;
    }

    @Override
    public List<?> step(UIQueryVisibility visibility) {
        currentVisibility = visibility;

        return null;
    }

    @Override
    public List step(UIQueryAST step) {
        return null;
    }

    public UIQueryDirection getCurrentDirection() {
        return currentDirection;
    }

    public UIQueryVisibility getCurrentVisibility() {
        return currentVisibility;
    }
}
