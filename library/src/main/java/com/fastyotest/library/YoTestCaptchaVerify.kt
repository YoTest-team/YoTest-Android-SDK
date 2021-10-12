package com.fastyotest.library

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.fastyotest.library.utils.VerifyUtils
import org.json.JSONObject

class YoTestCaptchaVerify(private var context: Activity, private var listener: YoTestListener?) {
    @SuppressLint("InflateParams")
    private val panel: View = LayoutInflater.from(context)
        .inflate(R.layout.include_yotest_captcha, null)
    private val webView: WebView = panel.findViewById(R.id.web_view)
    private val loadingPanel: ConstraintLayout = panel.findViewById(R.id.loading_panel)
    private val animationDrawable: AnimationDrawable

    init {
        val imgLoading: ImageView = panel.findViewById(R.id.img_loading)
        animationDrawable = imgLoading.background as AnimationDrawable
        (context.findViewById(android.R.id.content) as ViewGroup).addView(panel)
        loadingPanel.setOnClickListener {
            cancel()
            listener?.onClose("用户主动关闭")
        }
        panel.visibility = View.GONE
        initWebView()
    }

    fun verify() {
        if (!YoTestCaptcha.initStatus()) {
            cancel()
            listener?.onError(-1, "init failed")
            return
        }

        panel.visibility = View.VISIBLE
        loadingPanel.visibility = View.VISIBLE
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
        webView.stopLoading()
        webView.loadUrl("")
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
                view?.evaluateJavascript("javascript:verify(${VerifyUtils.buildScriptParams()})") {
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
                        return VerifyUtils.checkInterceptRequest(
                            context.externalCacheDir,
                            YoTestCaptcha.getInitResponse()!!.lib,
                            "text/javascript"
                        )
                    }
                    url.contains(YoTestCaptcha.getInitResponse()!!.binary) -> {
                        return VerifyUtils.checkInterceptRequest(
                            context.externalCacheDir,
                            YoTestCaptcha.getInitResponse()!!.binary,
                            "application/json"
                        )
                    }
                    url.contains(YoTestCaptcha.getInitResponse()!!.localWebview) -> {
                        return VerifyUtils.checkInterceptRequest(
                            context.externalCacheDir,
                            YoTestCaptcha.getInitResponse()!!.localWebview,
                            "text/html"
                        )
                    }
                }
                return null
            }
        }
    }

    private fun hideLoading() {
        if (animationDrawable.isRunning) {
            animationDrawable.stop()
        }
        loadingPanel.visibility = View.GONE
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
                    Toast.makeText(context, "已通过友验智能验证", Toast.LENGTH_SHORT).show()
                    listener?.onSuccess(
                        data?.optString("token")!!,
                        data.optBoolean("verified")
                    )
                }
                "onError" -> panel.post {
                    listener?.onError(data?.optInt("code")!!, data.optString("message"))
                }
                "onClose" -> panel.post {
                    cancel()
                    listener?.onClose(data?.toString())
                }
            }
        }
    }
}