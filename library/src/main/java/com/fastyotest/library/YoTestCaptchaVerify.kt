package com.fastyotest.library

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import org.json.JSONObject
import java.io.File

/**
 * Description: 展示验证码并进行验证
 * todo 改成添加一个layout文件，设置灰色蒙层
 * todo verify之后展示loading，onReady之后，取消loading
 * Created by: 2021/9/28 11:09 上午
 * Author: chendan
 */
class YoTestCaptchaVerify(private var context: Activity, private var listener: YoTestListener?) {
    private val webView: WebView = WebView(context)

    init {
        initWebView()
    }

    fun verify() {
        if (!YoTestCaptcha.initStatus()) {
            Log.d(TAG, "init failed")
            webView.visibility = View.GONE
            listener?.onError(-1, "init failed")
            return
        }
        webView.settings.apply {
            userAgentString += "YoTest_Android/${YoTestCaptcha.getInitResponse()!!.version}"
        }
        webView.visibility = View.VISIBLE
        webView.addJavascriptInterface(YoTestJSBridge(), "YoTestCaptcha")
        webView.loadUrl(YoTestCaptcha.getInitResponse()!!.webview)
    }

    fun cancel() {
        webView.removeJavascriptInterface("YoTestCaptcha")
        webView.visibility = View.GONE
    }

    /**
     * 销毁资源
     */
    fun onDestroy() {
        webView.destroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView.setBackgroundColor(0)
        webView.background?.alpha = 0
        webView.visibility = View.GONE
        (context.findViewById(android.R.id.content) as ViewGroup).addView(webView)
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

    private inner class YoTestJSBridge {

        @JavascriptInterface
        fun call(input: String) {
            val jsonObject = JSONObject(input)
            val action = jsonObject.optString("action")
            val data = jsonObject.optJSONObject("data")
            when (action) {
                "onReady" -> webView.post {
                    listener?.onReady(data?.toString())
                }
                "onSuccess" -> webView.post {
                    webView.visibility = View.GONE
                    listener?.onSuccess(
                        data?.optString("token")!!,
                        data.optBoolean("verified")
                    )
                }

                "onError" -> webView.post {
                    webView.visibility = View.GONE
                    listener?.onError(data?.optInt("code")!!, data.optString("message"))
                }
                "onClose" -> webView.post {
                    webView.visibility = View.GONE
                    listener?.onClose(data?.toString())
                }
            }
        }
    }

    companion object {
        private const val TAG = "YoTestCaptchaVerify"
    }
}