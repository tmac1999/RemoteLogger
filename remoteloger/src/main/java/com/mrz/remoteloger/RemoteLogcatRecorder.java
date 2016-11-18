
package com.mrz.remoteloger;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.GetCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * log日志统计保存
 *
 * @author way
 */

public class RemoteLogcatRecorder {


    private static final String TAG = "RemoteLogger";
    private static RemoteLogcatRecorder INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPId;
    /**
     * 上传形式，文件或者一行一行上传
     */
    @Deprecated
    int uploadType;

    /**
     * 上传的地址
     */
    String uploadUrl;
    FactorType factorType;
    String AVOSAppId;
    String AVOSAppKey;
    String factor;
    /**
     * 当上传 的是文件时，文件限制大小
     */

    int Upload_file_size;
    /**
     * 是否需要加密
     */
    boolean shouldEncrypt;


    /**
     * 初始化目录
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


    private RemoteLogcatRecorder(Builder builder) {

        this.factor = builder.factor;
        this.uploadUrl = builder.uploadUrl;
        this.factorType = builder.factorType;
        this.shouldEncrypt = builder.shouldEncrypt;
        this.Upload_file_size = builder.Upload_file_size;
        this.uploadType = builder.uploadType;
        this.AVOSAppId = builder.AVOSAppId;
        this.AVOSAppKey = builder.AVOSAppKey;
        mPId = android.os.Process.myPid();
    }

    public enum FactorType {
        USERNAME, EASEMOB_ID, IMEI, BUTTON
    }

    //=======================================================================Builder Start
    public static final class Builder {
        /**
         * 逐行上传容易出现先后顺序错乱，obsolete
         */
        @Deprecated
        public static final int UPLOAD_BY_LINE = 1;
        public static final int UPLOAD_TYPE_FILE = 2;
        FactorType factorType;
        String factor;
        /**
         * 上传形式，文件或者一行一行上传
         */
        int uploadType;
        /**
         * 上传的地址
         */
        String uploadUrl;
        String AVOSAppId;
        String AVOSAppKey;
        /**
         * 当上传 的是文件时，文件限制大小
         */

        int Upload_file_size;
        /**
         * 是否需要加密
         */
        boolean shouldEncrypt;

        /**
         * default setting
         */
        public Builder() {
            uploadType = UPLOAD_BY_LINE;
            uploadUrl = null;
            Upload_file_size = -1;
            shouldEncrypt = false;
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
         * @param factor if factor type isnt {@link RemoteLogcatRecorder.FactorType#BUTTON} ,it will be used for  named the server database table and comparison and will determine whether or not  start log.
         *               <p>         if factor type is {@link RemoteLogcatRecorder.FactorType#BUTTON} ,it will be used for  named the server database table
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
         * <p>NOTE：if already builded（which means  RemoteLogcatRecorder already exist ）,this will reuse the same instance with  new  config(ignore AVOSAppId,AVOSAppKey),wont create more log file or log thread.
         */
        public RemoteLogcatRecorder build() {
            if (INSTANCE == null) {
                INSTANCE = new RemoteLogcatRecorder(this);
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
                String  mLogDumpernotnull = "BUTTON switch to open ! remote log started!";
                String  mLogDumpernull = "mLogDumper not null !!";
                start(context,factor,mLogDumpernotnull,mLogDumpernull);
                break;
        }
    }

    private void start(Context context ,String factor,String mLogDumpernotnull, String mLogDumpernull) {
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT, factor);
            AVOSService.uploadDeviceInfo(context, factor);
            mLogDumper.start();
            Log.d("doStartLog", mLogDumpernotnull);
        } else {
            Log.d("doStartLog", mLogDumpernull);
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
                    String mLogDumpernull = "factor match! remote log has already started and wont start again!";
                    String mLogDumpernotnull = "factor match! remote log started!";
                    start(context,user_factor,mLogDumpernotnull,mLogDumpernull);
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
        // AVOSCloud.initialize(context,"PdT80DTSvAdKfFhHqjn37mBD-gzGzoHsz","vhpfjQXjx7bJDrn8OyMSpwsu");
        startWithoutInit(context);
    }

    public void startWithoutInit(Context context) {
        if (uploadType == Builder.UPLOAD_TYPE_FILE) {
            initFile(context);
        }
        shouldStart(context);

    }

    public void stop() {
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
         * 一次上传多少行
         */
        private int UPLOAD_LINE_NUM = 5;

        /**
         * TODO 保证线程对象唯一，不然上传多份日志
         *
         * @param pid
         * @param dir
         * @param factor
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
             * 日志等级：*:v , *:d ,  *:i，*:w , *:e , *:f , *:s    VERBOSE、DEBUG、INFO、WARN、ERROR FATAL
             *
             * 显示当前mPID程序的 E和W等级的日志.
             *grep（global search regular expression(RE) and print out the line，全面搜索正则表达式并把行打印出来）
             * */

            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
            cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
            // cmds = "logcat -s way";//打印标签过滤信息
            // cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";  //输出e w i级别的日志
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
                                AVOSService.upload(factor, encrypt);
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
//                                    AVOSService.upload(factor, stringBuilder.toString());
//                                    stringBuilder.delete(0,stringBuilder.length()-1);
//                                }
                                AVOSService.upload(factor, writeLine);
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


