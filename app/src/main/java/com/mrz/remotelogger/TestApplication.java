package com.mrz.remotelogger;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.mrz.remoteloger.core.RemoteLogcatRecorder;

/**
 * Created by zhengpeng on 2016/11/17.
 */
public class TestApplication extends Application {
    public static String AVOSAppId = "PdT80DTSvAdKfFhHqjn37mBD-gzGzoHsz";
    public static String AVOSAppKey = "vhpfjQXjx7bJDrn8OyMSpwsu";
    final String cmds = "logcat  | grep \"(" + android.os.Process.myPid() + ")\"";

    @Override
    public void onCreate() {
        super.onCreate();
        new RemoteLogcatRecorder.Builder()
                .factorType(RemoteLogcatRecorder.FactorType.BUTTON)
                .factor(getDeviceId(this))
                // .factor("vhpfjQXjx7bJDrn8OyMSpwsu")
                .AVOSAppId(AVOSAppId)
                .AVOSAppKey(AVOSAppKey)
                .logCmd(cmds)
                .uploadType(RemoteLogcatRecorder.Builder.UpLoadType.UPLOAD_BY_LINE_FILE)
//                .uploadFileSize(1024)
//                .shouldEncrypt(true)
//                .uploadUrl("www.baidu.com/log")
                .build().startWithInit(this);
    }

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
    public static boolean isEmpty(String s) {
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

    public static String getUniqueID() {
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
}
