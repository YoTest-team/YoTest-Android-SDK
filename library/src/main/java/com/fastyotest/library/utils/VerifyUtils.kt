package com.fastyotest.library.utils

import android.webkit.WebResourceResponse
import com.fastyotest.library.YoTestCaptcha
import org.json.JSONObject
import java.io.File

/**
 * Description:  工具
 * Created by: 2021/10/11 10:01 上午
 * Author: chendan
 */
object VerifyUtils {
    fun checkInterceptRequest(
        parentFile: File?,
        fileName: String,
        mineType: String
    ): WebResourceResponse? {
        if (fileName.startsWith("https")) {
            return null
        }

        try {
            val file = File(parentFile, fileName)
            if (file.exists()) {
                val inputStream = file.inputStream()
                return WebResourceResponse(mineType, "UTF-8", inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun buildScriptParams(): String {
        val jsonObject = JSONObject()
        jsonObject.put("accessId", YoTestCaptcha.getAccessId())
        jsonObject.put("platform", "android")
        jsonObject.put("product", "bind")
        return jsonObject.toString()
    }
}