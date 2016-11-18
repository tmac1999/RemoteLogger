package com.mrz.remotelogger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.mrz.remoteloger.RemoteLogcatRecorder;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RemoteLogger";
    // Content View Elements

    private EditText et_username;
    private EditText et_password;
    private EditText et_url;
    private TextView tv_result;
    private WebView wv;

    // End Of Content View Elements

    private void bindViews() {

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_url = (EditText) findViewById(R.id.et_url);
        tv_result = (TextView) findViewById(R.id.tv_result);
        wv = (WebView) findViewById(R.id.wv);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
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
                    .factorType(RemoteLogcatRecorder.FactorType.USERNAME)
                    .factor(username)
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

}
