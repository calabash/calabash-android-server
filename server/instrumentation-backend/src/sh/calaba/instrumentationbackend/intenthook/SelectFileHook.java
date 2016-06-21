package sh.calaba.instrumentationbackend.intenthook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import sh.calaba.instrumentationbackend.FakePickerActivity;
import sh.calaba.instrumentationbackend.InstrumentationBackend;

public class SelectFileHook extends IntentHookWithDefault {
    @Override
    public IntentHookResult defaultHook(Activity target, Intent intent) {
        if (intent.resolveActivity(InstrumentationBackend.instrumentation.getTargetContext().getPackageManager()) != null) {
            Intent modifiedIntent = new Intent(intent);

            Context instrumentationContext = InstrumentationBackend.instrumentation.getContext();
            String pkg = instrumentationContext.getPackageName();

            modifiedIntent.setComponent(new ComponentName(pkg, FakePickerActivity.class.getName()));

            return new IntentHookResult(null, false, modifiedIntent);
        } else {
            return new IntentHookResult(null, false);
        }
    }
}
