package sh.calaba.instrumentationbackend.actions.device;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class UiautomatorDragCoordinates implements Action {
    @Override
    public Result execute(String... args) {

        Integer fromX = new Integer(args[0]);
        Integer fromY = new Integer(args[1]);
        Integer toX = new Integer(args[2]);
        Integer toY = new Integer(args[3]);
        Integer stepCount;

        if (args.length > 4) {
            stepCount = new Integer(args[4]);
        } else {
            stepCount = 40;
        }

        InstrumentationBackend.getUiDevice().drag(fromX, toX, fromY, toY, stepCount);

        return new Result(true);
    }

    @Override
    public String key() {
        return "uiautomator_drag_coordinates";
    }
}
