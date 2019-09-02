package sh.calaba.instrumentationbackend.actions.device;

import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * Created by rajdeepvarma on 10/12/16.
 */
public class UiautomatorScreenDump implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();
        String dumpXml = "";
        File dumpView = null;
        try {
            dumpView = File.createTempFile("window_dump", "uix");
            mDevice.dumpWindowHierarchy(dumpView);
            FileInputStream fin = new FileInputStream(dumpView);
            dumpXml = convertStreamToString(fin);
            fin.close();
            dumpView.delete();
        } catch (IOException e) {
            Log.e("UI dump", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("UI dump", e.getMessage());
            e.printStackTrace();
        } finally {
            if(dumpView != null) dumpView.delete();
        }


        return new Result(true, dumpXml);
    }

    @Override
    public String key() {
        return "uiautomator_ui_dump";
    }

    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
}
