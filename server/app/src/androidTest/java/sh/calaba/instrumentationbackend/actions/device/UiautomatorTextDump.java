package sh.calaba.instrumentationbackend.actions.device;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * Created by rajdeepvarma on 10/12/16.
 */
public class UiautomatorTextDump implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.uiDevice;
        BySelector selector = By.clazz("android.widget.TextView");
        List<UiObject2> mList = mDevice.findObjects(selector);

        ArrayList<String> list = new ArrayList<String>();

        for (UiObject2 ob : mList) {
            list.add(ob.getText());
        }

        return new Result(true, list);
    }

    @Override
    public String key() {
        return "uiautomator_text_dump";
    }
}
