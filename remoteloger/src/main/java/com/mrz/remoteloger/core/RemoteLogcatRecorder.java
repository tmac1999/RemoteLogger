
package com.mrz.remoteloger.core;

import android.content.Context;
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
import java.util.LinkedList;

/**
 * log recorder
 */

public class RemoteLogcatRecorder {


    private static final String TAG = "RemoteLogger";
    private static RemoteLogcatRecorder INSTANCE = null;
    //private static String PATH_LOGCAT = Environment.getExternalStorageDirectory() + File.separator + "RomoteLogger" + File.separator;

    private String fileName;
    private static LogDumper mLogDumper = null;
    private int mPId;
    /**
     * <p>type as{@link RemoteLogcatRecorder.Builder.UpLoadType#UPLOAD_BY_LINE}
     * <p>type as{@link RemoteLogcatRecorder.Builder.UpLoadType#UPLOAD_BY_FILE}
     */
    Builder.UpLoadType uploadType;

    /**
     * uploadByLine to your server url
     */
    String uploadUrl;
    FactorType factorType;
    String AVOSAppId;
    String AVOSAppKey;
    String factor;
    /**
     * if you set {@link Builder.UpLoadType#UPLOAD_BY_FILE},you could set your file size
     */

    int Upload_file_size;
    /**
     * whether or not encrypt
     */
    boolean shouldEncrypt;
    private String username;
    private int lineCount;
    private String cmd;
    private Context context;


    private RemoteLogcatRecorder(Builder builder) {
        this.factor = builder.factor;
        this.uploadUrl = builder.uploadUrl;
        this.factorType = builder.factorType;
        this.shouldEncrypt = builder.shouldEncrypt;
        this.Upload_file_size = builder.Upload_file_size;
        this.uploadType = builder.uploadType;
        this.AVOSAppId = builder.AVOSAppId;
        this.AVOSAppKey = builder.AVOSAppKey;
        this.username = builder.username;
        this.lineCount = builder.lineCount;
        this.cmd = builder.cmd;
        mPId = android.os.Process.myPid();
    }

    /**
     *
     */
    public enum FactorType {
        /**
         * USERNAME来触发
         */
        USERNAME,
        /**
         * EASEMOB_ID来触发
         */
        EASEMOB_ID,
        /**
         * IMEI来触发
         */
        IMEI,
        /**
         * 按钮触发，在onClick事件中调用startWithoutInit触发日志记录
         */
        BUTTON,

    }

    //=======================================================================Builder Start
    public static final class Builder {
       public enum UpLoadType {
            /**
             * NOTE:this maybe cause log sequence disorder，
             */
            UPLOAD_BY_LINE,
            /**
             * 按文件上传，文件按字节数统计大小
             */
            UPLOAD_BY_FILE,
            /**
             * 按文件上传，文件按行数统计大小
             */
            UPLOAD_BY_LINE_FILE
        }


        private FactorType factorType;
        private String factor;
        private UpLoadType uploadType;
        private String uploadUrl;
        private String AVOSAppId;
        private String AVOSAppKey;

        private int Upload_file_size;
        private int lineCount = 1000;
        private boolean shouldEncrypt;
        private String username;
        private String cmd;

        /**
         * default setting
         */
        public Builder() {
            uploadType = UpLoadType.UPLOAD_BY_LINE;
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

        /**
         * @param kb 单位kb
         * @return Builder
         */
        @Deprecated
        public Builder uploadFileSize(int kb) {
            if (kb < 1 || kb > 5 * 1024) {
                throw new IllegalArgumentException("size too large or too small");
            }
            Upload_file_size = kb;
            return this;
        }

        /**
         * @param lineCount 最大行数  default is 1000
         * @return Builder
         */
        public Builder uploadFileMaxLine(int lineCount) {
            if (lineCount < 100 || lineCount > 100 * 1000) {
                throw new IllegalArgumentException("wrong lineCount ");
            }
            this.lineCount = lineCount;
            return this;
        }

        /**
         * define upload type,if default,it will upload line by line automatically,if set{@link RemoteLogcatRecorder.Builder.UpLoadType#UPLOAD_BY_FILE}or{@link RemoteLogcatRecorder.Builder.UpLoadType#UPLOAD_BY_LINE_FILE}
         * ,you should explicit invoke {@link RemoteLogcatRecorder#doUploadLogs}
         * <p>default is{@link RemoteLogcatRecorder.Builder.UpLoadType#UPLOAD_BY_LINE}
         * <p>type as{@link RemoteLogcatRecorder.Builder.UpLoadType#UPLOAD_BY_FILE}
         * <p>type as{@link RemoteLogcatRecorder.Builder.UpLoadType#UPLOAD_BY_LINE_FILE}
         */
        public Builder uploadType(UpLoadType uploadType) {
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

        /**
         * <p>NOTE：factorType should not be null.if factor,factorType match with server,it will create a log file,and start log.
         *
         * @param factorType 触发日志开始记录的因子形式
         * @return Builder
         */
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
         * @param cmd cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
         * @return 通过命令可以自定义 日志输出等级 默认："logcat *:e *:w  | grep \"(" + mPID + ")\""; 输出e w
         */
        public Builder logCmd(String cmd) {
            this.cmd = cmd;
            return this;
        }

        /**
         * <p>NOTE：factorType should not be null.if factor,factorType match with server,it will create a log file,and start log.
         * <p>if factor is null,it will immediatly return and do nothing.
         * <p>NOTE：if already builded（which means  RemoteLogcatRecorder already exist ）,this will create a new RemoteLogcatRecorder instance with  new  config(not include AVOSAppId,AVOSAppKey).
         */
        public RemoteLogcatRecorder build() {
            INSTANCE = new RemoteLogcatRecorder(this);
            return INSTANCE;
        }
    }

    //=======================================================================Builder end

    String PATH_LOGCAT;

    /**
     * init log file dir
     * TODO context null point
     */
    public FileOutputStream initFile(Context context) {

        PATH_LOGCAT = context.getFilesDir().getAbsolutePath()
                + File.separator + "RomoteLogger" + File.separator;
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            fileName = "t" +
                    TimeUtils.getDateENddHHmmss() + ".txt";
            out = new FileOutputStream(new File(PATH_LOGCAT, fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
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
            mLogDumper = new LogDumper(context, String.valueOf(mPId), factor);
            if (uploadType == Builder.UpLoadType.UPLOAD_BY_LINE) {
                //UPLOAD_BY_LINE时，不需要手动调用doUploadLogs，因此要在一开始记录时就上传info
                AVOSService.uploadDeviceInfo(context, factor, username);
            }
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
     * 三条log记录的时间也会顺移
     * <p>2016-11-23 10:33:21 D/RemoteLogger( 9060): did you see me in back end ? 0
     * <p>2016-11-23 10:33:28 D/RemoteLogger( 9060): did you see me in back end ? 0
     * <p>2016-11-23 10:33:39 D/RemoteLogger( 9060): did you see me in back end ? 0
     * 这一点与logcat类似，手机插入时，会先大量打印插入之前(数分钟？)的log.
     * 因此尽量避免stop后又重新start,每次start都会新开一个线程对象logDumper
     *
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
        this.context = context;
        AVOSCloud.initialize(context, AVOSAppId, AVOSAppKey);
        startWithoutInit(context);
    }

    public void startWithoutInit(Context context) {
//        if (uploadType == Builder.UPLOAD_BY_FILE) {
//            initFile(context);
//        }
        this.context = context;
        shouldStart(context);
    }

    public static void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
            Log.d(TAG, "RemoteLogger has stopped !");
        }
    }

    private FileOutputStream out = null;
    private LinkedList<String> lines = new LinkedList<String>();

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;

        private String factor;

        /**
         * @param pid
         * @param factor
         */
        public LogDumper(Context context, String pid, String factor) {
            mPID = pid;
            if (uploadType == Builder.UpLoadType.UPLOAD_BY_FILE) {
                initFile(context);
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
            if (cmd == null) {
                cmds = "logcat *:e *:w  | grep \"(" + mPID + ")\"";  //print level "e w i" log
            } else {
                cmds = cmd;
            }
            this.factor = factor;
        }


        private void stopLogs() {
            mRunning = false;
        }


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
                            doRecordorUpload(encrypt);

                        } else {
                            writeLine = writeLine + "\n";
                            doRecordorUpload(writeLine);
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

        private void doRecordorUpload(String line) throws IOException {
            if (out != null && uploadType == Builder.UpLoadType.UPLOAD_BY_FILE) {
                out.write(line.getBytes());
            } else if (uploadType == Builder.UpLoadType.UPLOAD_BY_LINE) {
                AVOSService.uploadByLine(factor, line);
            } else if (uploadType == Builder.UpLoadType.UPLOAD_BY_LINE_FILE) {
                lines.add(line);
                if (lines.size() > lineCount) {
                    lines.removeFirst();
                }
            }
        }

    }

    /**
     * {@link Builder#uploadType}必须为{@link Builder.UpLoadType#UPLOAD_BY_FILE}或者
     * {@link Builder.UpLoadType#UPLOAD_BY_LINE_FILE}
     */
    public void doUploadLogs() {
        if (uploadType == Builder.UpLoadType.UPLOAD_BY_LINE) {
            throw new RuntimeException("uploadType not set or uploadType is UPLOAD_BY_LINE");
        }
        FileOutputStream out = initFile(context);
        if (lines != null) {
            for (String line : lines) {
                try {
                    out.write(line.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        AVOSService.uploadFile(context, factor+".txt", PATH_LOGCAT + fileName, null, null);
        //  某些factorType 可以在start时，上传info 某些则在点击(doUploadLogs)时上传info
        AVOSService.uploadDeviceInfo(context, factor, username);
    }
    public static RemoteLogcatRecorder getInstance(){
        return INSTANCE;
    }
}


