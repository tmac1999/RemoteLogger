package com.mrz.remoteloger;

import android.content.Context;
import android.os.Build;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;

public class AVOSService {
    /**
     * Error code indicating a missing or invalid classname. Classnames are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters.
     * @param factor They must start with a letter, and a-zA-Z0-9_ are the only valid characters.
     * @param line 上传行内容
     */
    public static void upload(String factor, String line) {
        AVObject doing = new AVObject("user_"+factor);
        doing.put("log_lines", line);
        doing.saveInBackground();
    }

    public static String FACTOR_TYPE_USERNAME = "FACTOR_TYPE_USERNAME";
    public static String FACTOR_TYPE_EASEMOBID = "FACTOR_TYPE_EASEMOBID";
    public static String FACTOR_TYPE_IMEI = "FACTOR_TYPE_IMEI";
    private static String FACTOR_TABLE = "factor";

    public static void getFactor( GetCallback<AVObject> getCallback) {
        String factor = null;
        AVQuery<AVObject> query = new AVQuery<>(FACTOR_TABLE);
      //  AVQuery<AVObject> avObjectAVQuery = query.selectKeys(Arrays.asList(type));
        query.getFirstInBackground(getCallback);
    }
//    public static void getFactor1(String type, GetCallback<AVObject> getCallback) {
//        String factor = null;
//        AVQuery<AVObject> query = new AVQuery<>("test");
//        //  AVQuery<AVObject> avObjectAVQuery = query.selectKeys(Arrays.asList(type));
//        query.getFirstInBackground(getCallback);
//    }
    public static void test(String imei){
        AVObject doing = new AVObject("test");
        doing.put("imei", imei);
        doing.saveInBackground();
    }
    public static void uploadDeviceInfo(Context c,String user_factor,String username){
        AVObject avObject = new AVObject("DeviceInfo");
        String deviceType = "BRAND=" + Build.BRAND + ",MODEL=" + Build.MODEL + ",SDK_INT=" + Build.VERSION.SDK_INT;
        avObject.put("deviceType", deviceType);
        avObject.put("networkType", NetWorkHelper.getInstance(c).getNetWorkType());
        avObject.put("user_factor", user_factor);
        avObject.put("imei", CommonUtils.getDeviceId(c));
        avObject.put("appversion", CommonUtils.getPackageVersionAndName(c));
        avObject.put("username", username);

        avObject.saveInBackground();
    }
}

