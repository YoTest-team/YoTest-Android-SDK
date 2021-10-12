package com.fastyotest.demo

import android.app.Application
import android.util.Log
import com.fastyotest.library.YoTestCaptcha

/**
 * Description:
 * Created by: 2021/10/11 4:24 下午
 * Author: chendan
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        YoTestCaptcha.init(
            this.applicationContext,
            "0ce98f5fc3f2ffbd731b6b8bbf9e4992"
        ) { code, message ->
            Log.d("MyApplication", "YoTestCaptcha init $message")
        }
    }
}