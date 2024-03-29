package com.fastyotest.library

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.fastyotest.library.utils.VerifyUtils
import org.json.JSONObject

class YoTestCaptchaVerify(private val activity: Activity, private val listener: YoTestListener?) {

    @SuppressLint("InflateParams")
    private val panel: View =
        LayoutInflater.from(activity).inflate(R.layout.include_yotest_captcha, null)
    private val webView: CustomWebView = panel.findViewById(R.id.web_view)
    private val loadingPanel: ConstraintLayout = panel.findViewById(R.id.loading_panel)
    private val animationDrawable: AnimationDrawable =
        panel.findViewById<ImageView>(R.id.img_loading).background as AnimationDrawable

    private var dialog: AlertDialog? = null
    var showLoading = true
    var showToast = true

    init {
        dialog = AlertDialog.Builder(activity)
            .setCancelable(false)
            .setView(panel)
            .create()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(0x00000000))
            requestFeature(Window.FEATURE_NO_TITLE)
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.resources.displayMetrics.heightPixels
            )
        }
        loadingPanel.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            activity.resources.displayMetrics.heightPixels
        )
        loadingPanel.setOnClickListener {
            hideVerify()
            listener?.onClose("用户主动关闭")
        }
        initWebView()
    }

    fun verify() {
        if (!YoTestCaptcha.initStatus()) {
            listener?.onError(-1, "init failed")
            return
        }
        if (!webView.settings.userAgentString.contains("YoTest")) {
            webView.settings.apply {
                userAgentString += " YoTest_Android/${YoTestCaptcha.getInitResponse()!!.version}"
            }
        }
        webView.addJavascriptInterface(YoTestJSBridge(), "YoTestCaptcha")
        webView.loadUrl(YoTestCaptcha.getInitResponse()!!.webview)

        dialog?.show()
        if (showLoading) {
            loadingPanel.visibility = View.VISIBLE
            animationDrawable.start()
        } else {
            loadingPanel.visibility = View.GONE
            dialog?.dismiss()
        }
    }

    fun destroy() {
        dialog?.setOnDismissListener {
            webView.destroy()
        }
        hideVerify()
    }

    private fun hideVerify() {
        hideLoading()
        webView.removeJavascriptInterface("YoTestCaptcha")
        webView.stopLoading()
        webView.visibility = View.GONE
        dialog?.dismiss()
    }

    private fun hideLoading() {
        if (animationDrawable.isRunning) {
            animationDrawable.stop()
        }
        loadingPanel.visibility = View.GONE
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        CookieManager.getInstance().setAcceptCookie(true)
        webView.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            activity.resources.displayMetrics.heightPixels
        )
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
                when {
                    url.contains(YoTestCaptcha.getInitResponse()!!.lib) -> {
                        return VerifyUtils.checkInterceptRequest(
                            activity.externalCacheDir,
                            YoTestCaptcha.getInitResponse()!!.lib,
                            "text/javascript"
                        )
                    }
                    url.contains(YoTestCaptcha.getInitResponse()!!.binary) -> {
                        return VerifyUtils.checkInterceptRequest(
                            activity.externalCacheDir,
                            YoTestCaptcha.getInitResponse()!!.binary,
                            "application/json"
                        )
                    }
                    url.contains(YoTestCaptcha.getInitResponse()!!.localWebview) -> {
                        return VerifyUtils.checkInterceptRequest(
                            activity.externalCacheDir,
                            YoTestCaptcha.getInitResponse()!!.localWebview,
                            "text/html"
                        )
                    }
                }
                return null
            }
        }
    }

    private inner class YoTestJSBridge {

        @JavascriptInterface
        fun call(input: String) {
            val jsonObject = JSONObject(input)
            val action = jsonObject.optString("action")
            val data = jsonObject.optJSONObject("data")
            when (action) {
                "onReady" -> activity.window.decorView.post {
                    listener?.onReady(data?.toString())
                }
                "onShow" -> activity.window.decorView.post {
                    hideLoading()
                    webView.visibility = View.VISIBLE
                    dialog?.let {
                        if (!it.isShowing) {
                            it.show()
                        }
                    }
                    listener?.onShow(data?.toString())
                }
                "onSuccess" -> activity.window.decorView.post {
                    hideVerify()
                    if (showToast) {
                        Toast.makeText(activity, "已通过友验智能验证", Toast.LENGTH_SHORT).show()
                    }
                    listener?.onSuccess(
                        data?.optString("token")!!,
                        data.optBoolean("verified")
                    )
                }
                "onError" -> activity.window.decorView.post {
                    listener?.onError(data?.optInt("code")!!, data.optString("message"))
                }
                "onClose" -> activity.window.decorView.post {
                    hideVerify()
                    listener?.onClose(data?.toString())
                }
            }
        }
    }
}