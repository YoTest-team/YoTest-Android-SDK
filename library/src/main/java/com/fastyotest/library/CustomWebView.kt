package com.fastyotest.library

import android.content.Context
import android.text.InputType.TYPE_CLASS_TEXT
import android.util.AttributeSet
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputConnection
import android.webkit.WebView


/**
 * Description:
 * Created by: 2021/11/12 4:30 下午
 * Author: chendan
 */
class CustomWebView : WebView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val baseInputConnection = BaseInputConnection(this, false)
        outAttrs.imeOptions = IME_ACTION_DONE
        outAttrs.inputType = TYPE_CLASS_TEXT
        return baseInputConnection
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

}