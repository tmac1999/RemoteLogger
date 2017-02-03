package com.mrz.remoteloger;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;

import java.io.IOException;

public class AVOSService {
    public static String FACTOR_TYPE_USERNAME = "FACTOR_TYPE_USERNAME";
    public static String FACTOR_TYPE_EASEMOBID = "FACTOR_TYPE_EASEMOBID";
    public static String FACTOR_TYPE_IMEI = "FACTOR_TYPE_IMEI";
    private static String FACTOR_TABLE = "factor";

    /**
     * Error code indicating a missing or invalid classname. Classnames are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters.
     *
     * @param factor They must start with a letter, and a-zA-Z0-9_ are the only valid characters.
     * @param line   log content line
     */
    public static void uploadByLine(String factor, String line) {
        AVObject doing = new AVObject("user_" + factor);
        doing.put("log_lines", line);
        doing.saveInBackground();
    }

//    /**
//     * Error code indicating a missing or invalid classname. Classnames are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters.
//     *
//     * @param factor They must start with a letter, and a-zA-Z0-9_ are the only valid characters.
//     * @param line   log content line
//     */
//    public static void uploadByFile(String factor, String line) {
//
//
//        AVObject doing = new AVObject("user_" + factor);
//        doing.put("log_lines", line);
//        doing.saveInBackground();
//    }


    public static void uploadFile(Context c, String fileName, String imgAbsoluteLocalPath, SaveCallback uploadCallback, ProgressCallback uploadProgressCallback) {
        AVFile avFile = null;
        try {
            avFile = AVFile.withAbsoluteLocalPath(fileName, imgAbsoluteLocalPath);
            String deviceType = "BRAND=" + Build.BRAND + ",MODEL=" + Build.MODEL + ",SDK_INT=" + Build.VERSION.SDK_INT;
            avFile.addMetaData("deviceType", deviceType);
            avFile.addMetaData("imei", CommonUtils.getDeviceId(c));
            avFile.addMetaData("appversion", CommonUtils.getPackageVersionAndName(c));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (avFile != null) {
            if (uploadCallback == null) {
                avFile.saveInBackground(new UploadCallback(avFile));
            } else {
                avFile.saveInBackground(uploadCallback, uploadProgressCallback);
            }
        }

    }

    static class UploadCallback extends SaveCallback {

        private final AVFile avFile;

        public UploadCallback(AVFile avFile) {
            this.avFile = avFile;
        }

        @Override
        public void done(AVException e) {
            int code = 0;
            if (e != null) {
                code = e.getCode();
                e.printStackTrace();
            }
            String url = avFile.getUrl();
            Log.d("done", "UploadCallback code=" + code + "url=" + url);
        }
    }

    public static void getFactor(GetCallback<AVObject> getCallback) {
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
    public static void test(String imei) {
        AVObject doing = new AVObject("test");
        doing.put("imei", imei);
        doing.saveInBackground();
    }

    public static void uploadDeviceInfo(Context c, String user_factor, String username) {
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

