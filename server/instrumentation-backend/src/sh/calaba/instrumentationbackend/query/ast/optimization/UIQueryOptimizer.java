package sh.calaba.instrumentationbackend.query.ast.optimization;

import android.view.View;

import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;
import sh.calaba.instrumentationbackend.query.ast.evaluation.UIQueryEvaluationStep;

public class UIQueryOptimizer extends UIQueryEvaluationStep implements QueryOptimizer {
    public UIQueryOptimizer(List<View> inputViews) {
        super(inputViews);
    }

    @Override
    public List<UIQueryAST> optimize(List<UIQueryAST> queryPath) {
        return null;
    }
}
