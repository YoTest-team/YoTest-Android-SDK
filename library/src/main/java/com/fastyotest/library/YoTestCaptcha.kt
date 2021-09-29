package com.fastyotest.library

import android.content.Context
import android.util.Log
import com.fastyotest.library.bean.InitResponse
import com.fastyotest.library.utils.NetworkUtils
import org.json.JSONObject
import java.io.File

/**
 * Description: 验证码
 * Created by: 2021/9/28 11:09 上午
 * Author: chendan
 */
object YoTestCaptcha {

    private const val TAG = "YoTestCaptcha"
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

    /**
     * 获取初始化数据，建议在app启动的时候进行调用
     */
    fun init(context: Context, accessId: String, onResult: (code: Int, message: String) -> Unit) {
        this.accessId = accessId
        this.applicationContext = context.applicationContext

        Thread {
            var initRepeatCount = 0
            while (initRepeatCount < 3) {
                val result = NetworkUtils.sendRequest(INIT_API)
                Log.d(TAG, "result = $result")
                if (!result.isNullOrEmpty()) {
                    // 解析数据
                    val jsonObject = JSONObject(result).optJSONObject("data")
                    initResponse = InitResponse(
                        checkProtocol(jsonObject?.optString("api")),
                        checkProtocol(jsonObject?.optString("lib")),
                        checkProtocol(jsonObject?.optString("binary")),
                        checkProtocol(jsonObject?.optString("webview")),
                        "",
                        checkProtocol(jsonObject?.optString("image")),
                        jsonObject?.optString("version") ?: ""
                    )
                    checkFile("webview", initResponse!!.webview)
                    checkFile("lib", initResponse!!.lib)
                    checkFile("binary", initResponse!!.binary)
                    Log.d(TAG, "init success")
                    onResult(0, "init success")
                    break
                }
                initRepeatCount++
            }

            if (initRepeatCount >= 3) {
                // init failed，回调结果给用户
                Log.d(TAG, "init failed")
                onResult(-1, "init failed")
            }

        }.start()
    }

    /**
     * @return ture:init success; false: init failed
     */
    fun initStatus(): Boolean {
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
                // 进行文件下载，下载成功之后，更新文件路径
                Thread {
                    if (NetworkUtils.downloadFile(downloadUrl, file)) {
                        Log.d(TAG, "download success: $file")
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
                Log.d(TAG, "cache valid: $file")
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