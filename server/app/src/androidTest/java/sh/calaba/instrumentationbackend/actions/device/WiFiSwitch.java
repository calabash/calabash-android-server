package sh.calaba.instrumentationbackend.actions.device;

import android.content.Context;
import android.net.wifi.WifiManager;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

/**
 * Created by rajdeepvarma on 08/01/17.
 */
public class WiFiSwitch implements Action {

    /**
     * Created by rajdeepvarma on 10/12/16.
     */
    @Override
    public Result execute(String... args) {
        String status = args[0];

        WifiManager wifiManager = (WifiManager) InstrumentationBackend.solo.getCurrentActivity().getSystemService(Context.WIFI_SERVICE);
        if (status.equals("on")) {
            if (wifiManager.isWifiEnabled()) {
                System.out.println("WifiEnabled ist bereits an");
            } else {
                if (wifiManager.setWifiEnabled(true))
                    System.out.println("WifiEnabled ist nun an");
            }
        } else {
            if (status.equals("off")){
                if (!wifiManager.isWifiEnabled()) {
                    System.out.println("WifiEnabled ist bereits aus");
                } else {
                    if (wifiManager.setWifiEnabled(false))
                        System.out.println("WifiEnabled ist nun aus");
                }
            }
        }

        return Result.successResult();
    }

    @Override
    public String key() {
        return "wifi";
    }
}

