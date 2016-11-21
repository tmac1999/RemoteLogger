package com.mrz.remoteloger;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetWorkHelper {
    private Context context;
    private volatile static NetWorkHelper helper;
    private NetWorkHelper(Context application) {
        context = application;
    }
    public static NetWorkHelper getInstance(Context context) {
        if (helper == null) {
            synchronized (NetWorkHelper.class) {
                if (helper == null) {
                    helper = new NetWorkHelper(context);
                }
            }
        }
        return helper;
    }
    public String getNetWorkType(){
        String typeName = null;
        try {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // mobile 3G Data Network
            typeName = conMan.getActiveNetworkInfo().getTypeName();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return typeName;
    }
}
