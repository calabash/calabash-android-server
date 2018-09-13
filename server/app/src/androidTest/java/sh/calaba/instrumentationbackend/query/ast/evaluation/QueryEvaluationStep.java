package sh.calaba.instrumentationbackend.query.ast.evaluation;

import java.util.List;

import sh.calaba.instrumentationbackend.query.ast.*;

public interface QueryEvaluationStep<T> {
    List<T> step(UIQueryDirection direction);
    List<T> step(UIQueryVisibility visibility);
    List<T> step(UIQueryAST step);
}
