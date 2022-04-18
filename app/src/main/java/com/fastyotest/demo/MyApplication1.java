package com.fastyotest.demo;

import android.app.Application;
import android.util.Log;

import com.fastyotest.library.YoTestCaptcha;

public class MyApplication1 extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        YoTestCaptcha.init(
                this.getApplicationContext(),
                "0ce98f5fc3f2ffbd731b6b8bbf9e4992",
                (integer, s) -> {
                    Log.d("MyApplication", "YoTestCaptcha init $message");
                    return null;
                }
        );
    }
}
