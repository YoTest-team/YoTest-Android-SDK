package com.fastyotest.library

import com.fastyotest.library.port.YoTestBaseListener

abstract class YoTestListener : YoTestBaseListener {
    override fun onReady(data: String?) {

    }

    override fun onShow(data: String?) {

    }

    override fun onSuccess(token: String, verified: Boolean) {

    }

    override fun onError(code: Int, message: String) {

    }

    override fun onClose(data: String?) {

    }
}