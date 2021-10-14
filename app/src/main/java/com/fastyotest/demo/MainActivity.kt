package com.fastyotest.demo

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fastyotest.demo.databinding.ActivityMainBinding
import com.fastyotest.library.YoTestCaptchaVerify
import com.fastyotest.library.YoTestListener

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Demo"
    }

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var yoTestCaptchaVerify: YoTestCaptchaVerify

    @ColorInt
    private var fontColor: Int = 0
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fontColor = ContextCompat.getColor(this, R.color.purple_500)

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
            viewBinding.txtCallback.append("====================\n")
            viewBinding.txtCallback.append(getSpannableString("onReady"))
            viewBinding.txtCallback.append(": $data\n")
        }

        override fun onShow(data: String?) {
            Log.d(TAG, "onShow: $data")
            viewBinding.txtCallback.append(getSpannableString("onShow"))
            viewBinding.txtCallback.append(": $data\n")
            dialog?.dismiss()
        }

        override fun onSuccess(token: String, verified: Boolean) {
            Log.d(TAG, "onSuccess: token=$token; verified=$verified")
            viewBinding.txtCallback.append(getSpannableString("onSuccess"))
            viewBinding.txtCallback.append(": token=$token; verified=$verified\n")
        }

        override fun onError(code: Int, message: String) {
            Log.d(TAG, "onError: code=$code; message=$message")
            viewBinding.txtCallback.append(getSpannableString("onError"))
            viewBinding.txtCallback.append(": code=$code; message=$message\n")
        }

        override fun onClose(data: String?) {
            Log.d(TAG, "onClose: $data")
            viewBinding.txtCallback.append(getSpannableString("onClose"))
            viewBinding.txtCallback.append(": $data\n")
        }
    }

    private fun getSpannableString(target: String): SpannableString {
        val spanString = SpannableString(target)
        val span = ForegroundColorSpan(fontColor)
        spanString.setSpan(span, 0, target.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spanString
    }

    override fun onDestroy() {
        super.onDestroy()
        yoTestCaptchaVerify.destroy()
    }
}