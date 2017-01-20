
package com.mrz.remoteloger.core;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.GetCallback;
import com.mrz.remoteloger.AVOSService;
import com.mrz.remoteloger.Constants;
import com.mrz.remoteloger.Encryption;
import com.mrz.remoteloger.TimeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * log recorder
 */

public class RemoteLogcatRecorderTest {


    private static final String TAG = "RemoteLogger";
    private static RemoteLogcatRecorderTest INSTANCE = null;
    private static String PATH_LOGCAT;
    private static LogDumper mLogDumper = null;
    private int mPId;
    /**
     * <p>type as{@link RemoteLogcatRecorderTest.Builder#UPLOAD_BY_LINE}
     * <p>type as{@link RemoteLogcatRecorderTest.Builder#UPLOAD_TYPE_FILE}
     */
    int uploadType;

    /**
     * uploadByLine to your server url
     */
    String uploadUrl;
    FactorType factorType;
    String AVOSAppId;
    String AVOSAppKey;
    String factor;
    /**
     * if you set {@link Builder#UPLOAD_TYPE_FILE},you could set your file size
     */

    int Upload_file_size;
    /**
     * whether or not encrypt
     */
    boolean shouldEncrypt;
    private String username;


    /**
     * init log file dir
     */
    public void initFile(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Constants.LOG_SDCARD_DIR;
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath()
                    + File.separator + "CnfolLog" + File.separator;
        }
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    private RemoteLogcatRecorderTest(Builder builder) {

        this.factor = builder.factor;
        this.uploadUrl = builder.uploadUrl;
        this.factorType = builder.factorType;
        this.shouldEncrypt = builder.shouldEncrypt;
        this.Upload_file_size = builder.Upload_file_size;
        this.uploadType = builder.uploadType;
        this.AVOSAppId = builder.AVOSAppId;
        this.AVOSAppKey = builder.AVOSAppKey;
        this.username = builder.username;
        mPId = android.os.Process.myPid();
    }

    public enum FactorType {
        USERNAME, EASEMOB_ID, IMEI, BUTTON
    }

    //=======================================================================Builder Start
    public static final class Builder {
        /**
         * NOTE:this maybe cause log sequence disorder，
         */
        public static final int UPLOAD_BY_LINE = 1;
        public static final int UPLOAD_TYPE_FILE = 2;
        private FactorType factorType;
        private String factor;
        private int uploadType;
        private String uploadUrl;
        private String AVOSAppId;
        private String AVOSAppKey;

        private int Upload_file_size;
        private boolean shouldEncrypt;
        private String username;

        /**
         * default setting
         */
        public Builder() {
            uploadType = UPLOAD_BY_LINE;
            uploadUrl = null;
            Upload_file_size = -1;
            shouldEncrypt = false;
        }

        /**
         * @param username optional ,username will be add in the device info table for record
         * @return builder
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder uploadFileSize(int kb) {
            if (kb < 1 || kb > 5 * 1024) {
                throw new IllegalArgumentException("size too large or too small");
            }
            Upload_file_size = kb;
            return this;
        }

        public Builder uploadType(int uploadType) {
            this.uploadType = uploadType;
            return this;
        }

        public Builder shouldEncrypt(boolean shouldEncrypt) {
            this.shouldEncrypt = shouldEncrypt;
            return this;
        }

        public Builder AVOSAppId(String AVOSAppId) {
            this.AVOSAppId = AVOSAppId;
            return this;
        }

        public Builder AVOSAppKey(String AVOSAppKey) {
            this.AVOSAppKey = AVOSAppKey;
            return this;
        }

        public Builder factorType(FactorType factorType) {
            this.factorType = factorType;
            return this;
        }

        /**
         * 上传的因子
         * 比如指定某一个用户名上传 Upload_factor = username
         * 比如指定某一个imei Upload_factor = imei
         * 比如某一个指令     =action
         * 比如某一类机型。。？
         *
         * @param factor if factor type isnt {@link RemoteLogcatRecorderTest.FactorType#BUTTON} ,it will be used for  named the server database table and comparison and will determine whether or not  start log.
         *               <p>         if factor type is {@link RemoteLogcatRecorderTest.FactorType#BUTTON} ,it will be used for  named the server database table
         **/
        public Builder factor(String factor) {
            this.factor = factor;
            return this;
        }

        public Builder uploadUrl(String uploadUrl) {
            this.uploadUrl = uploadUrl;
            return this;
        }

        /**
         * <p>NOTE：factorType should not be null.if factor,factorType match with server,it will create a log file,and start log.
         * <p>if factor is null,it will immediatly return and do nothing.
         * <p>NOTE：if already builded（which means  RemoteLogcatRecorder already exist ）,this will reuse the same instance with  new  config(ignore AVOSAppId,AVOSAppKey).
         */
        public RemoteLogcatRecorderTest build() {
            if (INSTANCE == null) {
                INSTANCE = new RemoteLogcatRecorderTest(this);
            } else {
                INSTANCE.setBuilder(this);
            }
            return INSTANCE;
        }
    }

    //=======================================================================Builder end
    private void setBuilder(Builder builder) {
        this.factor = builder.factor;
        this.uploadUrl = builder.uploadUrl;
        this.factorType = builder.factorType;
        this.shouldEncrypt = builder.shouldEncrypt;
        this.Upload_file_size = builder.Upload_file_size;
        this.uploadType = builder.uploadType;
        this.username = builder.username;
    }


    private void shouldStart(Context context) {
        if (factor == null) {
            return;
        }
        if (factorType == null) {
            throw new NullPointerException("factorType should not be null");
        }
        switch (factorType) {
            case USERNAME:
                doStartLog(AVOSService.FACTOR_TYPE_USERNAME, factor, context);
                break;
            case EASEMOB_ID:
                doStartLog(AVOSService.FACTOR_TYPE_EASEMOBID, factor, context);
                break;
            case IMEI:
                doStartLog(AVOSService.FACTOR_TYPE_IMEI, factor, context);
                break;
            case BUTTON:
                String mLogDumpernull = "BUTTON switch to open ! remote log started!";
                String mLogDumpernotnull = "mLogDumper not null !!";
                start(context, factor, mLogDumpernotnull, mLogDumpernull);
                break;
        }
    }

    private void start(Context context, String factor, String mLogDumpernotnull, String mLogDumpernull) {
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT, factor);
            AVOSService.uploadDeviceInfo(context, factor, username);
            mLogDumper.start();
            Log.d("doStartLog", mLogDumpernull);
        } else {
            Log.d("doStartLog", mLogDumpernotnull);
        }
    }

    private void doStartLog(final String factorType, final String user_factor, final Context context) {

        AVOSService.getFactor(new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e != null)
                    e.printStackTrace();
                Log.d("doStartLog", avObject + "===");
                String server_factor = avObject.getString(factorType);
                if (user_factor != null && user_factor.equals(server_factor)) {
                    String mLogDumpernotnull = "factor match! remote log has already started and wont start again!";
                    String mLogDumpernull = "factor match! remote log started!";
                    start(context, user_factor, mLogDumpernotnull, mLogDumpernull);
                } else {

                    if (mLogDumper != null) {
                        Log.d("doStartLog", "factor dont match! remote log stopped !" + "  mRunning" + mLogDumper.mRunning);
                    } else {
                        Log.d("doStartLog", "factor dont match! remote log not started !");
                    }
                    stop();
                }
            }
        });
//            AVOSService.getFactor1(null, new GetCallback<AVObject>() {
//                @Override
//                public void done(AVObject avObject, AVException e) {
//                    if (e!=null)
//                    e.printStackTrace();
//                    Log.d("doStartLog",avObject+"===");
//                    String factor = avObject.getString("imei");
//                    Log.d("doStartLog","=================="+factor);
//
//                }
//            });
    }

    /**
     * 反复开关时，可能会因为第二次打开会记录下之前的内容并上传。
     * 比如 start 1 2 3 stop 4 5 6 start 7 8 stop start 9 10
     * 可能会上传为
     * <p>start 1 2 3       start 1 2 3 stop 4 5 6 start 7 8      start 1 2 3 stop 4 5 6 start 7 8 stop start 9 10
     *  三条log记录的时间也会顺移
     * <p>2016-11-23 10:33:21 D/RemoteLogger( 9060): did you see me in back end ? 0
     * <p>2016-11-23 10:33:28 D/RemoteLogger( 9060): did you see me in back end ? 0
     * <p>2016-11-23 10:33:39 D/RemoteLogger( 9060): did you see me in back end ? 0
     * 这一点与logcat类似，手机插入时，会先大量打印插入之前(数分钟？)的log.
     * 因此尽量避免stop后又重新start,每次start都会新开一个线程对象logDumper
     * @param context activity or application
     */
    public void startWithInit(Context context) {
        // 初始化参数依次为 this, AppId, AppKey
        /**
         * 我们每个月提供 100 万次的免费额度，超过的部分才收费。推送服务和统计服务免费使用，并不占用免费额度。
         +
         默认情况下，每个应用同一时刻的并发请求上限为 30（即同一时刻最多可以同时处理 30 个数据请求）。
         我们会根据应用运行状况以及运维需要调整改值。
         如果需要提高这一上限，请写信至 support@leancloud.cn 进行申请。
         */
        AVOSCloud.initialize(context, AVOSAppId, AVOSAppKey);
        startWithoutInit(context);
    }
    public void startWithoutInit(Context context) {
        if (uploadType == Builder.UPLOAD_TYPE_FILE) {
            initFile(context);
        }
        shouldStart(context);

    }

    public static void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
            Log.d(TAG, "RemoteLogger has stopped !");
        }
    }

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;
        private String factor;
        /**
         * how many lines to be uploadByLine one time
         */
        private int UPLOAD_LINE_NUM = 5;

        /**
         *
         * @param pid
         * @param dir
         * @param factor
         * TODO cmds加入配置
         */
        public LogDumper(String pid, String dir, String factor) {
            mPID = pid;
            if (uploadType == Builder.UPLOAD_TYPE_FILE) {
                try {
                    String fileName = "t" +
                            TimeUtils.getDateENddHHmmss() + "." + System.currentTimeMillis();
                    out = new FileOutputStream(new File(dir, fileName));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            /**
             *
             * log level：*:v , *:d ,  *:i，*:w , *:e , *:f , *:s    VERBOSE、DEBUG、INFO、WARN、ERROR FATAL
             *
             *grep（global search regular expression(RE) and print out the line，）
             * */

            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
           // cmds = "logcat  | grep \"(" + mPID + ")\"";//print all level log
            // cmds = "logcat -s way";//print filter tag log
             cmds = "logcat *:e *:w  | grep \"(" + mPID + ")\"";  //print level "e w i" log
            this.factor = factor;
        }


        private void stopLogs() {
            mRunning = false;
        }

        //        StringBuilder stringBuilder = new StringBuilder();
        @Override
        public void run() {
//            int count = 1;
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (line.contains(mPID)) {
                        String writeLine = TimeUtils.getDateEN() + "  " + line;

                        if (shouldEncrypt) {
                            String encrypt = null;
                            try {
                                encrypt = Encryption.encrypt(writeLine, Constants.SALT) + "\n";
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (out != null && uploadType == Builder.UPLOAD_TYPE_FILE) {
                                out.write(encrypt.getBytes());
                            } else if (uploadType == Builder.UPLOAD_BY_LINE) {
                                AVOSService.uploadByLine(factor, encrypt);
                            }

                        } else {
                            if (out != null && uploadType == Builder.UPLOAD_TYPE_FILE) {
                                out.write(writeLine.getBytes());
                            } else if (uploadType == Builder.UPLOAD_BY_LINE) {
//                                if (count<UPLOAD_LINE_NUM){
//                                    stringBuilder.append(writeLine+"\r\n");
//                                    count++;
//                                }else{
//                                    count =0;
//                                    stringBuilder.append(writeLine);
//                                    AVOSService.uploadByLine(factor, stringBuilder.toString());
//                                    stringBuilder.delete(0,stringBuilder.length()-1);
//                                }
                                AVOSService.uploadByLine(factor, writeLine);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }

            }

        }

    }

}


