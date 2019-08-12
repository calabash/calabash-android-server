package com.jayway.android.robotium.solo;

import java.util.List;

import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.PointF;

public class SoloEnhanced extends Solo {
	public SoloEnhanced(Instrumentation instrumentation) {
		super(instrumentation);
	}

    public ActivityUtils getActivityUtils() {
        return activityUtils;
    }

    public void doubleTapOnScreen(float x, float y) {
        clicker.clickOnScreen(x,y);
        clicker.clickOnScreen(x,y);
    }


    public void drag_without_hiding_keyboard(float fromX, float toX, float fromY, float toY,
                     int stepCount) {
        scroller.drag(fromX, toX, fromY, toY, stepCount);
    }

    public void runOnMainSync(Runnable runner) {
        instrumentation.runOnMainSync(runner);
    }
}
