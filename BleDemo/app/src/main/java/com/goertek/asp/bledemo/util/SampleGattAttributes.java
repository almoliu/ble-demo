package com.goertek.asp.bledemo.util;

import java.util.HashMap;

/**
 * Created by almo.liu on 2017/5/24.
 */

public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String UUID_HR_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String UUID_DEVICE_INFORMATION_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";

    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_MEASUREMENT = "0000180f-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put(UUID_HR_SERVICE, "Heart Rate Service");
        attributes.put(UUID_DEVICE_INFORMATION_SERVICE, "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(CLIENT_CHARACTERISTIC_CONFIG,"client characteristic configuration");
        attributes.put(BATTERY_MEASUREMENT,"Battery Measurement");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
