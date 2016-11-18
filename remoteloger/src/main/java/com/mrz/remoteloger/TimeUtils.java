package com.mrz.remoteloger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengpeng on 2016/11/4.
 */
public class TimeUtils {



    /**
     * 获取系统当前日期时间 yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getDateEN() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());
        return date;
    }
    /**
     * 获取系统当前日期时间 yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getDateENddHHmmss() {
        SimpleDateFormat format = new SimpleDateFormat("dd HH:mm:ss");
        String date = format.format(new Date());
        return date;
    }
}
