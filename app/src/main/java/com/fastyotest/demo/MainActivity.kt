package com.fastyotest.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
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
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        yoTestCaptchaVerify = YoTestCaptchaVerify(this, yoTestListener)
        viewBinding.btnLogin.setOnClickListener {
            login()
        }
        viewBinding.showLoading.setOnCheckedChangeListener { _, isChecked ->
            yoTestCaptchaVerify.showLoading = isChecked
        }
        viewBinding.showToast.setOnCheckedChangeListener { _, isChecked ->
            yoTestCaptchaVerify.showToast = isChecked
        }
    }

    private fun login() {
        if (!yoTestCaptchaVerify.showLoading) {
            dialog = AlertDialog.Builder(this).setMessage("加载中...").show()
        }
        yoTestCaptchaVerify.verify()
    }

    private val yoTestListener = object : YoTestListener() {
        override fun onReady(data: String?) {
            Log.d(TAG, "onReady: $data")
        }

        override fun onShow(data: String?) {
            Log.d(TAG, "onShow: $data")
            dialog?.dismiss()
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