package com.fastyotest.library.utils

import java.io.*
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Description: 网络请求
 * Created by: 2021/9/28 4:36 下午
 * Author: chendan
 */
object NetworkUtils {

    fun sendRequest(api: String): String? {
        var inputStream: InputStream? = null
        var bufferedReader: BufferedReader? = null
        var connection: HttpsURLConnection? = null

        try {
            val url = URL(api)
            connection = url.openConnection() as HttpsURLConnection
            connection.apply {
                readTimeout = 5000
                requestMethod = "GET"
            }
            inputStream = connection.inputStream
            bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuffer = StringBuffer()
            var singLineData: String?
            while ((bufferedReader.readLine().also { singLineData = it }) != null) {
                stringBuffer.append(singLineData)
            }
            return stringBuffer.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedReader?.close()
            inputStream?.close()
            connection?.disconnect()
        }

        return null
    }

    /**
     * 下载文件
     * @param url 文件下载地址
     * @param file 缓存的文件
     */
    fun downloadFile(url: String, file: File): Boolean {
        var contentLength = 0L
        var conn: HttpURLConnection? = null
        var bis: BufferedInputStream? = null
        var raf: RandomAccessFile? = null
        var len: Int
        val buffer = ByteArray(1024 * 8)
        try {
            raf = RandomAccessFile(file, "rwd")
            conn = URL(url).openConnection() as HttpsURLConnection
            conn.connectTimeout = 5000
            val start: Long = file.length()
            //设置请求属性，请求制定范围的文件流
            conn.setRequestProperty("Range", "bytes=$start-")
            contentLength = conn.contentLength.toLong()
            //移动RandomAccessFile写入位置，从上次完成的位置开始
            raf.seek(start)
            bis = BufferedInputStream(conn.inputStream)
            while (-1 != bis.read(buffer).also { len = it }) {
                raf.write(buffer, 0, len)
            }
            return true
        } catch (e: ProtocolException) {
            try {
                raf!!.close()
                bis?.close()
                conn?.disconnect()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            if (file.length() != contentLength) {
                downloadFile(url, file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                raf?.close()
                bis?.close()
                conn?.disconnect()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        return false
    }
}