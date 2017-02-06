package com.mrz.remotelogger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mrz.remoteloger.core.RemoteLogcatRecorder;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RemoteLogger";
    // Content View Elements

    private EditText et_username;
    private EditText et_password;
    private EditText et_url;
    private TextView tv_result;
    private WebView wv;
    private ToggleButton tbtn_open_log;
    private RemoteLogcatRecorder recorder;

    // End Of Content View Elements

    private void bindViews() {

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_url = (EditText) findViewById(R.id.et_url);
        tv_result = (TextView) findViewById(R.id.tv_result);
        tbtn_open_log = (ToggleButton) findViewById(R.id.tbtn_open_log);
        wv = (WebView) findViewById(R.id.wv);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        final String cmds = "logcat  | grep \"(" + android.os.Process.myPid() + ")\"";
        tbtn_open_log.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
//                .shouldEncrypt(true)
//                .uploadUrl("www.baidu.com/log")
                    recorder = new RemoteLogcatRecorder.Builder()
                            .factorType(RemoteLogcatRecorder.FactorType.BUTTON)
                            .factor(TestApplication.getDeviceId(getApplicationContext()))
                            //.factor("vhpfjQXjx7bJDrn8OyMSpwsu")
                            .uploadType(RemoteLogcatRecorder.Builder.UpLoadType.UPLOAD_BY_LINE_FILE)
                            .logCmd(cmds)
                            .uploadFileMaxLine(150)
//                .uploadFileSize(1024)
//                .shouldEncrypt(true)
//                .uploadUrl("www.baidu.com/log")
                            .build();
                    recorder.startWithoutInit(getApplicationContext());
                } else {
                    RemoteLogcatRecorder.stop();
                }
            }
        });
    }

    public void login(View v) {
        String pwd = et_password.getText().toString();
        String username = et_username.getText().toString();
        mockLogin(username, pwd);
    }

    private void mockLogin(String username, String pwd) {
        boolean loginSuccess = true;
        if (loginSuccess) {
            new RemoteLogcatRecorder.Builder()
                    .factorType(RemoteLogcatRecorder.FactorType.BUTTON)
                    .factor(username)
                    .uploadType(RemoteLogcatRecorder.Builder.UpLoadType.UPLOAD_BY_LINE_FILE)
//                .uploadFileSize(1024)
//                .shouldEncrypt(true)
//                .uploadUrl("www.baidu.com/log")
                    .build().startWithoutInit(this);
        }
    }

    int count = 0;

    public void printlog(View v) {

        Log.d(TAG, "did you see me in  back end ?    " + count);
        count++;
    }

    public void uploadlog(View v) {
//        recorder.doUploadLogs();
        RemoteLogcatRecorder.getInstance().doUploadLogs();
    }

}
