package com.fastyotest.library

import android.content.Context
import com.fastyotest.library.bean.InitResponse
import com.fastyotest.library.utils.NetworkUtils
import org.json.JSONObject
import java.io.File

object YoTestCaptcha {
    private const val INIT_API = "https://api.fastyotest.com/api/init"
    private var accessId: String = ""
    private var initResponse: InitResponse? = null
    private var applicationContext: Context? = null

    internal fun getAccessId(): String {
        return this.accessId
    }

    internal fun getInitResponse(): InitResponse? {
        return this.initResponse
    }

    @JvmStatic
    fun init(context: Context, accessId: String, onResult: (code: Int, message: String) -> Unit) {
        this.accessId = accessId
        this.applicationContext = context.applicationContext

        Thread {
            var initRepeatCount = 0
            while (initRepeatCount < 3) {
                val result = NetworkUtils.sendRequest(INIT_API)
                if (!result.isNullOrEmpty()) {
                    val jsonObject = JSONObject(result).optJSONObject("data")
                    initResponse = InitResponse(
                        checkProtocol(jsonObject?.optString("api")),
                        checkProtocol(jsonObject?.optString("lib")),
                        checkProtocol(jsonObject?.optString("binary")),
                        checkProtocol(jsonObject?.optString("webview")),
                        localWebview = "",
                        checkProtocol(jsonObject?.optString("image")),
                        version = jsonObject?.optString("version") ?: ""
                    )
                    checkFile("webview", initResponse!!.webview)
                    checkFile("lib", initResponse!!.lib)
                    checkFile("binary", initResponse!!.binary)
                    onResult(0, "init success")
                    break
                }
                initRepeatCount++
            }

            if (initRepeatCount >= 3) {
                onResult(-1, "init failed")
            }

        }.start()
    }

    internal fun initStatus(): Boolean {
        if (initResponse == null || initResponse?.webview.isNullOrEmpty()) {
            return false
        }

        return true
    }

    private fun checkFile(type: String, downloadUrl: String) {
        if (downloadUrl.isEmpty()) {
            return
        }
        val fileName = getFileName(downloadUrl)
        applicationContext?.let {
            val file = File(it.externalCacheDir, fileName)
            if (!file.exists()) {
                Thread {
                    if (NetworkUtils.downloadFile(downloadUrl, file)) {
                        when (type) {
                            "lib" -> initResponse?.lib = fileName
                            "binary" -> initResponse?.binary = fileName
                            "webview" -> initResponse?.localWebview = fileName
                        }
                    } else {
                        file.delete()
                    }
                }.start()
            } else {
                when (type) {
                    "lib" -> initResponse?.lib = fileName
                    "binary" -> initResponse?.binary = fileName
                    "webview" -> initResponse?.localWebview = fileName
                }
            }
        }
    }

    private fun getFileName(url: String): String {
        val list = url.split("/")
        return if (list.isNotEmpty()) {
            list.last()
        } else {
            url
        }
    }

    private fun checkProtocol(input: String?): String {
        if (input.isNullOrEmpty()) {
            return ""
        }

        return if (input.startsWith("https:")) {
            input
        } else {
            "https:$input"
        }
    }

}