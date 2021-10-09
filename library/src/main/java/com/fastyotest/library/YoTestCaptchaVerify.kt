package com.fastyotest.library

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.Toast
import org.json.JSONObject
import java.io.File

/**
 * Description: 展示验证码并进行验证
 * 添加一个layout文件，设置灰色蒙层
 * verify之后展示loading，onReady之后，取消loading
 * Created by: 2021/9/28 11:09 上午
 * Author: chendan
 */
class YoTestCaptchaVerify(private var context: Activity, private var listener: YoTestListener?) {
    @SuppressLint("InflateParams")
    private val panel: View = LayoutInflater.from(context)
        .inflate(R.layout.include_yotest_captcha, null)
    private val webView: WebView = panel.findViewById(R.id.web_view)
    private val imgLoading: ImageView = panel.findViewById(R.id.img_loading)
    private val animationDrawable: AnimationDrawable = imgLoading.background as AnimationDrawable

    init {
        (context.findViewById(android.R.id.content) as ViewGroup).addView(panel)
        panel.visibility = View.GONE
        initWebView()
    }

    fun verify() {
        if (!YoTestCaptcha.initStatus()) {
            Log.d(TAG, "init failed")
            cancel()
            listener?.onError(-1, "init failed")
            return
        }

        panel.visibility = View.VISIBLE
        imgLoading.visibility = View.VISIBLE
        animationDrawable.start()
        webView.settings.apply {
            userAgentString += "YoTest_Android/${YoTestCaptcha.getInitResponse()!!.version}"
        }
        webView.addJavascriptInterface(YoTestJSBridge(), "YoTestCaptcha")
        webView.loadUrl(YoTestCaptcha.getInitResponse()!!.webview)
    }

    fun isShow(): Boolean {
        return panel.visibility == View.VISIBLE
    }

    fun cancel() {
        hideLoading()
        webView.removeJavascriptInterface("YoTestCaptcha")
        panel.visibility = View.GONE
    }

    /**
     * 销毁资源
     */
    fun onDestroy() {
        cancel()
        webView.destroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.setBackgroundColor(0)
        webView.background?.alpha = 0
        webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.post {
                    listener?.onReady("onPageFinished")
                }

                view?.evaluateJavascript("javascript:verify(${buildScriptParams()})") {
                    Log.d("onPageFinished", "evaluateJavascript received:$it")
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                url: String?
            ): WebResourceResponse? {
                if (url.isNullOrEmpty()) {
                    return null
                }
                // 替换成本地的资源
                when {
                    url.contains(YoTestCaptcha.getInitResponse()!!.lib) -> {
                        return checkInterceptRequest(
                            YoTestCaptcha.getInitResponse()!!.lib, "text/javascript"
                        )
                    }
                    url.contains(YoTestCaptcha.getInitResponse()!!.binary) -> {
                        return checkInterceptRequest(
                            YoTestCaptcha.getInitResponse()!!.binary, "application/json"
                        )
                    }
                    url.contains(YoTestCaptcha.getInitResponse()!!.localWebview) -> {
                        return checkInterceptRequest(
                            YoTestCaptcha.getInitResponse()!!.localWebview, "text/html"
                        )
                    }
                }
                return null
            }
        }
    }

    private fun checkInterceptRequest(
        newUrl: String,
        mineType: String
    ): WebResourceResponse? {
        if (newUrl.startsWith("https")) {
            return null
        }

        try {
            val file = File(context.externalCacheDir, newUrl)
            if (file.exists()) {
                val inputStream = file.inputStream()
                return WebResourceResponse(mineType, "UTF-8", inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun buildScriptParams(): String {
        val jsonObject = JSONObject()
        jsonObject.put("accessId", YoTestCaptcha.getAccessId())
        jsonObject.put("platform", "android")
        jsonObject.put("product", "bind")
        return jsonObject.toString()
    }

    private fun hideLoading() {
        if (animationDrawable.isRunning) {
            animationDrawable.stop()
        }
        imgLoading.visibility = View.GONE
    }

    private inner class YoTestJSBridge {

        @JavascriptInterface
        fun call(input: String) {
            val jsonObject = JSONObject(input)
            val action = jsonObject.optString("action")
            val data = jsonObject.optJSONObject("data")
            when (action) {
                "onReady" -> panel.post {
                    listener?.onReady(data?.toString())
                }
                "onShow" -> panel.post {
                    hideLoading()
                }
                "onSuccess" -> panel.post {
                    cancel()
                    Toast.makeText(context, "验证已通过", Toast.LENGTH_SHORT).show()
                    listener?.onSuccess(
                        data?.optString("token")!!,
                        data.optBoolean("verified")
                    )
                }
                "onError" -> panel.post {
                    cancel()
                    listener?.onError(data?.optInt("code")!!, data.optString("message"))
                }
                "onClose" -> panel.post {
                    cancel()
                    listener?.onClose(data?.toString())
                }
            }
        }
    }

    companion object {
        private const val TAG = "YoTestCaptchaVerify"
    }
}