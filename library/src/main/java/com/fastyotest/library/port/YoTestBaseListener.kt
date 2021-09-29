package com.fastyotest.library.port

/**
 * Description: 验证码结果回调
 * Created by: 2021/9/28 11:03 上午
 * Author: chendan
 */
interface YoTestBaseListener {
    /**
     * 当验证码准备就绪即将展现时回调
     */
    fun onReady(data: String?)

    /**
     * 当验证码验证成功后回调
     * @param token 验证成功的凭证，后端根据此凭证验证是否验证通过
     * @param verified 用户是否验证成功，调用者用于判断
     */
    fun onSuccess(token: String, verified: Boolean)

    /**
     * 当验证码验证失败/错误后回调
     * @param code 错误码
     * @param message 错误详细
     */
    fun onError(code: Int, message: String)

    /**
     * 当验证码关闭后回调
     */
    fun onClose(data: String?)
}