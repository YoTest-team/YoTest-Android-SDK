package com.fastyotest.library.bean

data class InitResponse(
    var api: String,
    var lib: String,
    var binary: String,
    var webview: String,
    var localWebview: String,
    var image: String,
    var version: String
)