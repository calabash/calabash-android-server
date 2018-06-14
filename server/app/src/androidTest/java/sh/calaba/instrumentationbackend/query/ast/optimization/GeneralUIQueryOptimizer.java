package sh.calaba.instrumentationbackend.query.ast.optimization;

import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;

public class GeneralUIQueryOptimizer implements QueryOptimizer {
    @Override
    public List<UIQueryAST> optimize(List<UIQueryAST> queryPath) {
        QueryOptimizer visibilityOptimizer = new UIQueryVisibilityOptimizer();
        QueryOptimizer redundancyOptimizer = new UIQueryRedundancyOptimizer();

        return redundancyOptimizer.optimize(visibilityOptimizer.optimize(queryPath));
    }
}
