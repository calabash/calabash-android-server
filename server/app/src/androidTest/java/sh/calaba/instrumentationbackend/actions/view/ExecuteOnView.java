package sh.calaba.instrumentationbackend.actions.view;

import java.util.List;

import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.query.Query;
import sh.calaba.instrumentationbackend.query.ui.UIObject;

/**
 * Created by john7doe on 06/01/15.
 */
public class ExecuteOnView {
    public Result execute(IOnViewAction onViewAction, String... args) {
        if (args.length != 1) {
            return Result.failedResult("Query for identifying view must be provided.");
        }

        final String message;

        try {
            Query query = new Query(args[0]);
            List<UIObject> uiObjects = query.uiObjectsForQuery();

            if (uiObjects.isEmpty()) {
                return Result.failedResult("Query found no view(s).");
            }

            message = onViewAction.getUIQueryMatcher(uiObjects.get(0)).call();
        } catch (Exception e) {
            return Result.fromThrowable(e);
        }

        return new Result(true, message);
    }
}
