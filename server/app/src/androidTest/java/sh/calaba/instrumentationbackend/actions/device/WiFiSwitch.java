package sh.calaba.instrumentationbackend.actions.device;

import android.content.Context;
import android.net.wifi.WifiManager;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class WiFiSwitch implements Action {

    @Override
    public Result execute(String... args) {
        String status = args[0];

        WifiManager wifiManager = (WifiManager) InstrumentationBackend.solo.getCurrentActivity().getSystemService(Context.WIFI_SERVICE);
        if (status.equals("on")) {
            if (wifiManager.isWifiEnabled()) {
                System.out.println("Wifi is already enabled");
            } else {
                if (wifiManager.setWifiEnabled(true))
                    System.out.println("Wifi is now enabled");
            }
        } else {
            if (status.equals("off")){
                if (!wifiManager.isWifiEnabled()) {
                    System.out.println("Wifi is already disabled");
                } else {
                    if (wifiManager.setWifiEnabled(false))
                        System.out.println("Wifi is now disabled");
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

