package com.fastyotest.library.port

interface YoTestBaseListener {

    fun onReady(data: String?)

    fun onShow(data: String?)

    fun onSuccess(token: String, verified: Boolean)

    fun onError(code: Int, message: String)

    fun onClose(data: String?)
}