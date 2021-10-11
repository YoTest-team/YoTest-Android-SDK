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
            "4297f44b13955235245b2497399d7a93"
        ) { code, message ->
            Log.d("MyApplication", "YoTestCaptcha init $message")
        }
    }
}