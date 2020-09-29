package sh.calaba.instrumentationbackend;

import android.os.Bundle;
import android.test.InstrumentationTestRunner;

import sh.calaba.instrumentationbackend.utils.MonoUtils;
import sh.calaba.instrumentationbackend.utils.StringUtils;

import static sh.calaba.instrumentationbackend.InstrumentationStatus.SUCCESSFUL;
import static sh.calaba.instrumentationbackend.InstrumentationStatus.FAILED;

public class ClearAppData3 extends InstrumentationTestRunner {

    private static final String DISPLAY_NAME = "ClearAppData3";

    @Override
    public void onCreate(Bundle arguments) {
        MonoUtils.loadMono(getTargetContext());

        try {
            Cleaner cleaner = new Cleaner();
            cleaner.clearAppData(getTargetContext());
        } catch (Exception e) {
            InstrumentationReport.send(this, DISPLAY_NAME, FAILED, StringUtils.toString(e));
            finish(FAILED.resultCode, null);
        }

        InstrumentationReport.send(this, DISPLAY_NAME, SUCCESSFUL, null);
        finish(SUCCESSFUL.resultCode, null);
    }
}
