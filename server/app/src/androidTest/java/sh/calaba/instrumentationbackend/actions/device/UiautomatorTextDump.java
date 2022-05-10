package sh.calaba.instrumentationbackend.actions.device;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import java.util.ArrayList;
import java.util.List;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class UiautomatorTextDump implements Action {
    @Override
    public Result execute(String... args) {
        UiDevice mDevice = InstrumentationBackend.getUiDevice();
        BySelector selector = By.clazz("android.widget.TextView");
        List<UiObject2> mList = mDevice.findObjects(selector);

        ArrayList<String> list = new ArrayList<>();

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
