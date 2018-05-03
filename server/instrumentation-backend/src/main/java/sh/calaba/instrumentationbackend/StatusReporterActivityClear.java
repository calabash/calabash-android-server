package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.os.Bundle;

public class StatusReporterActivityClear extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new StatusReporter(this).clear();

        finish();
    }
}
