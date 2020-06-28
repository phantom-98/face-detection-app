package com.facecoolalert.resources.thermal;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class DeviceTempUtils {

    public static Double getDeviceTemperature(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);

        int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
//        System.out.println("temperature : "+temperature);
        return temperature/10.0;
    }

}
