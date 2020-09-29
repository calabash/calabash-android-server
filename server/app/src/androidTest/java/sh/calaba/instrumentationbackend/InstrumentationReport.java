package sh.calaba.instrumentationbackend;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class InstrumentationReport {

    public static final String REPORT_STATUS = "status";
    public static final String REPORT_MESSAGE = "message";

    /**
     * Sends current status of instrumentation
     * @param instr - instrumentation to send status for
     * @param displayName - unique name of instrumentation (recommended - simple class name)
     * @param status - current status of instrumentation
     * @param message - any additional information
     */
    public static void send(Instrumentation instr, String displayName,
                                  InstrumentationStatus status, @Nullable String message) {
        Bundle results = new Bundle();

        results.putString(compositeKey(displayName, REPORT_STATUS), status.name());
        results.putString(compositeKey(displayName, REPORT_MESSAGE), !TextUtils.isEmpty(message) ? message : "no message");

        instr.sendStatus(status.resultCode, results);
    }

    private static String compositeKey(String displayName, String key) {
        return displayName + "-" + key;
    }
}
