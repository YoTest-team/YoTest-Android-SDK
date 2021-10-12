package com.fastyotest.library

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleObserver
import com.fastyotest.library.utils.VerifyUtils
import org.json.JSONObject

class YoTestCaptchaVerifyDialog : DialogFragment(), LifecycleObserver {

    private var webView: WebView? = null
    private var loadingPanel: ConstraintLayout? = null
    private var animationDrawable: AnimationDrawable? = null
    private var actionListener: YoTestListener? = null

    fun setActionClickListener(listener: YoTestListener) {
        this.actionListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.include_yotest_captcha, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imgLoading: ImageView = view.findViewById(R.id.img_loading)
        animationDrawable = imgLoading.background as AnimationDrawable
        webView = view.findViewById(R.id.web_view)
        loadingPanel = view.findViewById(R.id.loading_panel)
        loadingPanel?.setOnClickListener {
            dismiss()
            actionListener?.onClose("用户主动关闭")
        }
        initWebView()
        verify()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (context == null || dialog == null || dialog!!.window == null) {
            return
        }

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0x00000000))
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.displayMetrics.heightPixels
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideLoading()
        webView?.removeJavascriptInterface("YoTestCaptcha")
        webView?.stopLoading()
        webView?.loadUrl("")
    }

    private fun verify() {
        if (!YoTestCaptcha.initStatus()) {
            actionListener?.onError(-1, "init failed")
            dismiss()
            return
        }
        loadingPanel?.visibility = View.VISIBLE
        animationDrawable?.start()
        webView?.settings?.apply {
            userAgentString += "YoTest_Android/${YoTestCaptcha.getInitResponse()!!.version}"
        }
        webView?.addJavascriptInterface(YoTestJSBridge(), "YoTestCaptcha")
        webView?.loadUrl(YoTestCaptcha.getInitResponse()!!.webview)
    }

    private fun hideLoading() {
        if (animationDrawable != null && animationDrawable!!.isRunning) {
            animationDrawable?.stop()
        }
        loadingPanel?.visibility = View.GONE
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView?.setBackgroundColor(0)
        webView?.background?.alpha = 0
        webView?.settings?.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.post {
                    actionListener?.onReady("onPageFinished")
                }

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
                            context?.externalCacheDir,
                            YoTestCaptcha.getInitResponse()!!.lib,
                            "text/javascript"
                        )
                    }
                    url.contains(YoTestCaptcha.getInitResponse()!!.binary) -> {
                        return VerifyUtils.checkInterceptRequest(
                            context?.externalCacheDir,
                            YoTestCaptcha.getInitResponse()!!.binary,
                            "application/json"
                        )
                    }
                    url.contains(YoTestCaptcha.getInitResponse()!!.localWebview) -> {
                        return VerifyUtils.checkInterceptRequest(
                            context?.externalCacheDir,
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
                "onReady" -> webView?.post {
                    actionListener?.onReady(data?.toString())
                }
                "onShow" -> webView?.post {
                    hideLoading()
                }
                "onSuccess" -> webView?.post {
                    Toast.makeText(context, "已通过友验智能验证", Toast.LENGTH_SHORT).show()
                    actionListener?.onSuccess(
                        data?.optString("token")!!,
                        data.optBoolean("verified")
                    )
                    dismiss()
                }
                "onError" -> webView?.post {
                    actionListener?.onError(data?.optInt("code")!!, data.optString("message"))
                }
                "onClose" -> webView?.post {
                    actionListener?.onClose(data?.toString())
                    dismiss()
                }
            }
        }
    }
}