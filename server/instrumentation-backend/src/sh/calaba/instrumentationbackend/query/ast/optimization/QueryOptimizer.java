package sh.calaba.instrumentationbackend.query.ast.optimization;

import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.UIQueryAST;

public interface QueryOptimizer {
    List<UIQueryAST> optimize(List<UIQueryAST> queryPath);
}
