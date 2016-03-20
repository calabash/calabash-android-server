package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.content.Intent;

public interface ApplicationLifeCycle {
    public Activity start(Intent startIntent);
}
