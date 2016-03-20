package sh.calaba.instrumentationbackend.query;

import static sh.calaba.instrumentationbackend.InstrumentationBackend.viewFetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

import sh.calaba.instrumentationbackend.query.antlr.UIQueryLexer;
import sh.calaba.instrumentationbackend.query.antlr.UIQueryParser;
import sh.calaba.instrumentationbackend.query.ast.*;
import sh.calaba.instrumentationbackend.query.ast.evaluation.QueryEvaluator;
import sh.calaba.instrumentationbackend.query.ast.evaluation.UIQueryEvaluationStep;
import sh.calaba.instrumentationbackend.query.ast.optimization.GeneralUIQueryOptimizer;
import sh.calaba.instrumentationbackend.query.ast.optimization.QueryOptimizationCache;
import sh.calaba.instrumentationbackend.query.ast.optimization.QueryOptimizer;
import sh.calaba.instrumentationbackend.query.ui.UIObject;
import sh.calaba.instrumentationbackend.query.ui.UIObjectView;

import android.view.View;

public class Query {

	private String queryString;
	@SuppressWarnings("rawtypes")
	private List operations;

	public Query(String queryString) {
		this.queryString = queryString;
		this.operations = Collections.EMPTY_LIST;
		if (this.queryString == null || this.queryString.trim().equals("")) {
			throw new IllegalArgumentException("Illegal query: "
					+ this.queryString);
		}
	}

	@SuppressWarnings("rawtypes")
	public Query(String queryString, List args) {
		this(queryString);
		this.operations = args;
	}

	public QueryResult executeQuery() {
        List<UIQueryAST> queryPath = parseQuery(this.queryString);
		List<UIQueryAST> optimizedQuery = QueryOptimizationCache.getCacheFor(this.queryString);

		if (optimizedQuery == null) {
			QueryOptimizer queryOptimizer = new GeneralUIQueryOptimizer();
			optimizedQuery = queryOptimizer.optimize(queryPath);
			QueryOptimizationCache.cache(this.queryString, optimizedQuery);
		}

				return UIQueryEvaluator.evaluateQueryWithOptions(optimizedQuery,
                UIObjectView.listOfUIObjects(rootViews()), parseOperations(this.operations));
	}

    // @todo: Remove this when we make next iteration on types. `executeQuery` should return
    // results that include the original UIObject.
	public List<UIObject> uiObjectsForQuery() {
        List<UIQueryAST> queryPath = parseQuery(this.queryString);
        List<UIQueryAST> optimizedQuery = QueryOptimizationCache.getCacheFor(this.queryString);

        if (optimizedQuery == null) {
            QueryOptimizer queryOptimizer = new GeneralUIQueryOptimizer();
            optimizedQuery = queryOptimizer.optimize(queryPath);
            QueryOptimizationCache.cache(this.queryString, optimizedQuery);
        }

        return QueryEvaluator.evaluateQueryForPath(queryPath,
                new UIQueryEvaluationStep(UIObjectView.listOfUIObjects(rootViews())));
    }

	@SuppressWarnings("rawtypes")
	public static List<Operation> parseOperations(List ops) {
		List<Operation> result = new ArrayList<Operation>(ops.size());
		for (Object o : ops) {
			Operation op = null;
			if (o instanceof Operation) {
				op = (Operation) o;												
			}
			else if (o instanceof String) {
				op = new PropertyOperation((String) o);	
			}
			else if (o instanceof Map) {
				Map mapOp = (Map) o;				
				String methodName = (String) mapOp.get("method_name");
				if (methodName == null) {
					throw new IllegalArgumentException("Trying to convert a Map without method_name to an operation. " + mapOp.toString());
				}
				List arguments = (List) mapOp.get("arguments");
				if (arguments == null) {
					throw new IllegalArgumentException("Trying to convert a Map without arguments to an operation. " + mapOp.toString());
				}
				op = new InvocationOperation(methodName, arguments);
			}
			result.add(op);								
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static List<UIQueryAST> parseQuery(String query) {
		UIQueryLexer lexer = new UIQueryLexer(new ANTLRStringStream(query));
		UIQueryParser parser = new UIQueryParser(new CommonTokenStream(lexer));

		UIQueryParser.query_return q;
		try {
			q = parser.query();
		} catch (RecognitionException e) {
			throw new InvalidUIQueryException(e.getMessage());
		}
		if (q == null) {
			throw new InvalidUIQueryException(query);
		}
		CommonTree rootNode = (CommonTree) q.getTree();
		List<CommonTree> queryPath = rootNode.getChildren();

		if (queryPath == null || queryPath.isEmpty()) {
			queryPath = Collections.singletonList(rootNode);
		}

		return mapUIQueryFromAstNodes(queryPath);
	}


	public static List<UIQueryAST> mapUIQueryFromAstNodes(List<CommonTree> nodes) {
		List<UIQueryAST> mapped = new ArrayList<UIQueryAST>(nodes.size());
		for (CommonTree t : nodes) {
			mapped.add(uiQueryFromAst(t));
		}
		return mapped;
	}

	public static UIQueryAST uiQueryFromAst(CommonTree step) {
		String stepType = UIQueryParser.tokenNames[step.getType()];
		switch (step.getType()) {
		case UIQueryParser.QUALIFIED_NAME:
			return UIQueryASTClassName.fromQualifiedClassName(step.getText());
		case UIQueryParser.NAME:
			return UIQueryASTClassName.fromSimpleClassName(step.getText());
		case UIQueryParser.WILDCARD:
			return UIQueryASTClassName.fromQualifiedClassName("android.view.View");


			
		case UIQueryParser.FILTER_COLON:
			return UIQueryASTWith.fromAST(step);
			
		case UIQueryParser.ALL:
			return UIQueryVisibility.ALL;	
			
		case UIQueryParser.VISIBLE:
			return UIQueryVisibility.VISIBLE;
			
		case UIQueryParser.BEGINPRED:
			return UIQueryASTPredicate.newPredicateFromAST(step);
		case UIQueryParser.DIRECTION:
			return UIQueryDirection.valueOf(step.getText().toUpperCase());			
			
		default:
			throw new InvalidUIQueryException("Unknown query: " + stepType
					+ " with text: " + step.getText());

		}

	}

    public List<View> rootViews() {
        return new ArrayList<View>(UIQueryUtils.getRootViews());
    }
}
