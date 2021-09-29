package com.fastyotest.library.bean

/**
 * Description: init api response
 * Created by: 2021/9/28 3:06 下午
 * Author: chendan
 */
data class InitResponse(
    var api: String,
    var lib: String,
    var binary: String,
    var webview: String,
    var localWebview: String,
    var image: String,
    var version: String
)