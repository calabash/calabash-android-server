package sh.calaba.instrumentationbackend.query.ui;

import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import sh.calaba.instrumentationbackend.query.ast.UIQueryUtils;

public class UIObjectView implements UIObject {
    private View view;

    public UIObjectView(View view) {
        this.view = view;
    }

    @Override
    public View getObject() {
        return view;
    }

    @Override
    public <V> Future<V> evaluateAsyncInMainThread(Callable<V> callable) throws Exception {
        FutureTask<V> futureTask = new FutureTask<V>(callable);
        UIQueryUtils.runOnViewThread(view, futureTask);

        return futureTask;
    }

    public static List<UIObjectView> listOfUIObjects(List<View> views) {
        List<UIObjectView> list = new ArrayList<UIObjectView>(views.size());

        for (View view : views) {
            list.add(new UIObjectView(view));
        }

        return list;
    }
}
