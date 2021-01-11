package sh.calaba.instrumentationbackend;

import android.app.Activity;

public enum InstrumentationStatus {
    SUCCESSFUL(Activity.RESULT_OK),
    FAILED(Activity.RESULT_CANCELED);

    final int resultCode;

    InstrumentationStatus(int resultCode) {
        this.resultCode = resultCode;
    }
}
