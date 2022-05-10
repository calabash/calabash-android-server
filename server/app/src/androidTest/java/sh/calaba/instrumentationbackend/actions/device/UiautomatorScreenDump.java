package sh.calaba.instrumentationbackend.actions.device;
import androidx.test.uiautomator.AccessibilityNodeInfoDumperCustom;
import androidx.test.uiautomator.UiDevice;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class UiautomatorScreenDump implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();
        String dumpXml = "";
        try {
            OutputStream stream = new ByteArrayOutputStream();
            AccessibilityNodeInfoDumperCustom.dumpWindowHierarchy(mDevice,stream);
            dumpXml = stream.toString();
        } catch (IOException e) {
            Log.e("UI dump", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("UI dump", e.getMessage());
            e.printStackTrace();
        }


        return new Result(true, dumpXml);
    }

    @Override
    public String key() {
        return "uiautomator_ui_dump";
    }
}
