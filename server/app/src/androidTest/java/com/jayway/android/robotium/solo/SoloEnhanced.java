package com.jayway.android.robotium.solo;

import java.util.List;

import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.PointF;

public class SoloEnhanced extends Solo {

	public SoloEnhanced(Instrumentation instrumentation, Activity activity) {
		super(instrumentation, activity);
	}
    public ActivityUtils getActivityUtils() {
        return activityUtils;
    }

    public void doubleTapOnScreen(float x, float y) {
        clicker.clickOnScreen(x,y);
        clicker.clickOnScreen(x,y);
    }

    public void runOnMainSync(Runnable runner) {
        instrumentation.runOnMainSync(runner);
    }
}
