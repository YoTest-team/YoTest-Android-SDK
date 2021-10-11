package com.fastyotest.demo

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.fastyotest.demo.databinding.ActivityMainBinding
import com.fastyotest.library.YoTestCaptcha
import com.fastyotest.library.YoTestCaptchaVerify
import com.fastyotest.library.YoTestCaptchaVerifyDialog
import com.fastyotest.library.YoTestListener

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Demo"
    }

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var yoTestCaptchaVerify: YoTestCaptchaVerify
    private lateinit var yoTestCaptchaDialog: YoTestCaptchaVerifyDialog
    private var count = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // 初始化
        viewBinding.btnInit.setOnClickListener {
            YoTestCaptcha.init(
                this.applicationContext,
                "4297f44b13955235245b2497399d7a93"
            ) { code, message ->
                viewBinding.root.post {
                    viewBinding.tvInitResult.append("init code: $code;   message: $message\n")
                }
            }
        }
        // activity add view
        yoTestCaptchaVerify = YoTestCaptchaVerify(this, yoTestListener)
        viewBinding.btnLogin.setOnClickListener {
            count = System.currentTimeMillis()
            viewBinding.tvInitResult.append("webview  VISIBLE\n")
            yoTestCaptchaVerify.verify()

        }
        // dialog
        yoTestCaptchaDialog =
            YoTestCaptchaVerifyDialog().apply { setActionClickListener(yoTestListener) }
        viewBinding.btnLoginDialog.setOnClickListener {
            // todo 弹窗
            yoTestCaptchaDialog.show(supportFragmentManager, "YoTestCaptchaVerifyDialog")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && yoTestCaptchaVerify.isShow()) {
            yoTestCaptchaVerify.cancel()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private val yoTestListener = object : YoTestListener() {
        override fun onReady(data: String?) {
            Log.d(TAG, "onReady: $data")
            viewBinding.tvInitResult.append("onReady $data  ${System.currentTimeMillis() - count}\n")
        }

        override fun onSuccess(token: String, verified: Boolean) {
            Log.d(TAG, "onSuccess: token=$token; verified=$verified")
            viewBinding.tvInitResult.append("onSuccess  ${System.currentTimeMillis() - count}\n")
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
        yoTestCaptchaVerify.onDestroy()
    }
}