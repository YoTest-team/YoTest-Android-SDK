package com.fastyotest.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fastyotest.demo.databinding.ActivityMainBinding
import com.fastyotest.library.YoTestCaptchaVerify
import com.fastyotest.library.YoTestListener

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Demo"
    }

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var yoTestCaptchaVerify: YoTestCaptchaVerify

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        yoTestCaptchaVerify = YoTestCaptchaVerify(this, yoTestListener)
        viewBinding.btnLogin.setOnClickListener {
            yoTestCaptchaVerify.verify()
        }

        val yoTestCaptchaVerify = YoTestCaptchaVerify(this, object : YoTestListener() {
            override fun onReady(data: String?) {
                Log.d(TAG, "onReady: $data")
            }
        })
        yoTestCaptchaVerify.verify()
    }

    private val yoTestListener = object : YoTestListener() {
        override fun onReady(data: String?) {
            Log.d(TAG, "onReady: $data")
        }

        override fun onShow(data: String?) {
            Log.d(TAG, "onShow: $data")
        }

        override fun onSuccess(token: String, verified: Boolean) {
            Log.d(TAG, "onSuccess: token=$token; verified=$verified")
        }

        override fun onError(code: Int, message: String) {
            Log.d(TAG, "onError: code=$code; message=$message")
        }

        override fun onClose(data: String?) {
            Log.d(TAG, "onClose: $data")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        yoTestCaptchaVerify.destroy()
    }
}