package com.mrz.remoteloger;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * Created by zhengpeng on 2016/11/17.
 */
public class CommonUtils {
    public static String getDeviceId(Context context) {
        PackageManager pm = context.getPackageManager();
        boolean flag = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_PHONE_STATE", "com.cnfol.group.activity"));
        StringBuilder deviceId = new StringBuilder();
        if (flag) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (isEmpty(imei)) {
                //针对小米4手机android6.0出现flag为true,但仍然拿不到DeviceId的情况。
                String uniqueID = getUniqueID();
                if (!isEmpty(uniqueID)) {
                    deviceId.append(uniqueID);
                    return deviceId.toString();
                }
            } else {
                deviceId.append(imei);
                return deviceId.toString();
            }

        } else {
            String uniqueID = getUniqueID();
            if (!isEmpty(uniqueID)) {
                deviceId.append(uniqueID);
                return deviceId.toString();
            }

        }
        return deviceId.toString();
    }
    /**
     * 判断字符串是否为空
     *
     * @param s
     * @return
     */
    private static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }
    /**
     * 获取唯一手机设备码（没开权限的时候）
     *
     * @return
     */

    private static String getUniqueID() {
        StringBuilder sb = new StringBuilder();
        sb.append("35").append(Build.BOARD.length() % 10).append(Build.BRAND.length() % 10)
                .append(Build.CPU_ABI.length() % 10).append(Build.DEVICE.length() % 10)
                .append(Build.DISPLAY.length() % 10).append(Build.HOST.length() % 10)
                .append(Build.ID.length() % 10).append(Build.MANUFACTURER.length() % 10)
                .append(Build.MODEL.length() % 10).append(Build.PRODUCT.length() % 10)
                .append(Build.TAGS.length() % 10).append(Build.TYPE.length() % 10)
                .append(Build.USER.length() % 10);
        return sb.toString();
    }


    public static String getPackageVersionAndName(Context c) {
        // 获取packagemanager的实例
        PackageManager packageManager = c.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(c.getPackageName(), 0);
            String version = packInfo.versionName;
            String packageName = packInfo.packageName;
            return packageName+version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}