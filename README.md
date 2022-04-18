YoTest-Android-SDK 文档
----
[![](https://jitpack.io/v/YoTest-team/YoTest-Android-SDK.svg)](https://jitpack.io/#YoTest-team/YoTest-Android-SDK)

> 基于虚拟机保护、设备特征识别和操作行为识别的新一代智能验证码，具备智能评分、抗Headless、模拟伪装、针对恶意设备自动提升验证难度等多项安全措施，帮助开发者减少恶意攻击导致的数字资产损失，强力护航业务安全。

* [仓库入口](https://github.com/YoTest-team/YoTest-Android-SDK#%E4%BB%93%E5%BA%93%E5%85%A5%E5%8F%A3)
* [兼容性](https://github.com/YoTest-team/YoTest-Android-SDK#%E5%85%BC%E5%AE%B9%E6%80%A7)
* [示例项目](https://github.com/YoTest-team/YoTest-Android-SDK#%E7%A4%BA%E4%BE%8B%E9%A1%B9%E7%9B%AE)
* [安装](https://github.com/YoTest-team/YoTest-Android-SDK#%E5%AE%89%E8%A3%85)
* [快速开始](https://github.com/YoTest-team/YoTest-Android-SDK#%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B)
* [API](https://github.com/YoTest-team/YoTest-Android-SDK#api)


### 仓库入口：
<a href="https://gitee.com/yo-test-team/yo-test-android-sdk"><img src="./images/gitee2.png" width="32px"/></a>
<a href="https://github.com/YoTest-team/YoTest-Android-SDK"><img src="./images/GitHub.png" width="32px"/></a>

### 兼容性
* Android 4.4+ （即API 19+）

### 示例项目

你可以通过Android Studio 4+打开本项目进行示例项目的预览和更改，具体文件请[点击此处](https://github.com/YoTest-team/YoTest-Android-SDK/tree/master/app/src/main/java/com/fastyotest/demo)。

### 安装

在工程根目录的build.gradle中添加

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
在使用的module的build.gradle文件中添加

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.github.YoTest-team:YoTest-Android-SDK:1.0.2'
}
```

### 快速开始

在application的onCreate中初始化SDK。

```kotlin
import com.fastyotest.library.YoTestCaptcha

YoTestCaptcha.init(
    this.applicationContext,
    "当前项目所属的accessId，可以在后台中进行获取及查看"
    ) { code, message ->
        Log.d("MyApplication", "YoTestCaptcha init $message")
}
```

在要使用的页面中添加：

```kotlin
import com.fastyotest.library.YoTestCaptchaVerify

// 设置监听事件
private val yoTestListener = object : YoTestListener() {
    override fun onReady(data: String?) {
        Log.d(TAG, "onReady: $data")
    }

    override fun onSuccess(token: String, verified: Boolean) {
        Log.d(TAG, "onSuccess: token=$token; verified=$verified")
    }

    override fun onError(code: Int, message: String) {
        Log.d(TAG, "onError: code=$code; message=$message")
    }

    override fun onClose(data: String?) {
        Log.d(TAG, "onClose: $data")
    }
}

// 初始化验证模块
private val yoTestCaptchaVerify = YoTestCaptchaVerify(this, yoTestListener)

// 进行验证
yoTestCaptchaVerify.verify()

// 在使用页面的生活周期方法中销毁资源
override fun onDestroy() {
    super.onDestroy()
    yoTestCaptchaVerify.destroy()
}

```


### API

YoTestCaptcha初始化函数

* [YoTestCaptcha.init(context, accessId, callback)](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestCaptchainitcontext-accessId-callback)

YoTestCaptchaVerify实例方法

* [YoTestCaptchaVerify(context, listener)](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestCaptchaVerifycontext-listener)
* [verify()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestCaptchaVerifyverify)
* [destroy()](https://github.com/YoTest-team/YoTest-Android-SDK#yotestcaptchaverifydestroy)

YoTestListener实例方法

* [onReady()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestListeneronreadydata)
* [onShow()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestListeneronshowdata)
* [onSuccess()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestListeneronSuccesstoken-verified)
* [onError()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestListeneronErrordatacode-message)
* [onClose()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestListeneronclosedata)

#### YoTestCaptcha.init(context, accessId, callback)
  - `context` \<Context\> 必填，ApplicationContext
  - `accessId ` \<String\> 必填，当前项目所属的accessId，可以在友验后台中进行相关获取及查看
  - `callback` \<Function\>
    - **code**: \<Int\> 错误码，其返回非0时，请做好错误处理
    - **message**: \<String\> 提示信息
  - `return:` Unit

一般情况下我们会将init方法放在Application onCreate时进行触发，如果有其他业务相关需求，**请一定确保init方法在verify方法之前调用完成**。

```kotlin
YoTestCaptcha.init(
    this.applicationContext,
    "当前项目所属的accessId，可以在后台中进行获取及查看"
    ) { code, message ->
        Log.d("MyApplication", "YoTestCaptcha init $message")
}
```

#### YoTestCaptchaVerify(context, listener)
- `context ` \<Activity\> 必填，当前activity
- `listener ` \<Object\> 非必填，回调验证各个状态
-`return`: this

用于初始化验证模块

```kotlin
yoTestCaptchaVerify = YoTestCaptchaVerify(this, yoTestListener)
```

#### YoTestCaptchaVerify.verify()
- `return`: Unit

用于将调起验证页面

```kotlin
yoTestCaptchaVerify.verify()
```

#### YoTestCaptchaVerify.destroy()
- `return`: Unit

用于销毁相关资源

```kotlin
yoTestCaptchaVerify.destroy()
```

#### YoTestListener.onReady(data)
- `data` \<String?\> 可以为空
- `return`: Unit

初始化成功的回调监听

```kotlin
val yoTestCaptchaVerify = YoTestCaptchaVerify(this, object : YoTestListener(){
    override fun onReady(data: String?) {
        Log.d(TAG, "onReady: $data")
    }
})
yoTestCaptchaVerify.verify()
```

#### YoTestListener.onShow(data)
- `data` \<String?\> 可以为空
- `return`: Unit

验证内容展现的回调监听

```kotlin
val yoTestCaptchaVerify = YoTestCaptchaVerify(this, object : YoTestListener(){
    override fun onShow(data: String?) {
        Log.d(TAG, "onShow: $data")
    }
})
yoTestCaptchaVerify.verify()
```

#### YoTestListener.onSuccess(token, verified)
- `token` \<String\> 当前验证的凭证，需要提交给后端来进行是否通过判断
- `verified` \<Boolean\> 是否验证成功
- `return`: Unit

验证成功的回调监听

```kotlin
val yoTestCaptchaVerify = YoTestCaptchaVerify(this, object : YoTestListener(){
    override fun onSuccess(token: String, verified: Boolean) {
        Log.d(TAG, "onSuccess: token=$token; verified=$verified")
    }
})
yoTestCaptchaVerify.verify()
```

#### YoTestListener.onError(code, message)
- `code` \<Int\> 错误码
- `message` \<String\> 错误相关信息
- `return`: Unit

验证错误的回调监听

```kotlin
val yoTestCaptchaVerify = YoTestCaptchaVerify(this, object : YoTestListener(){
    override fun onError(code: Int, message: String) {
        Log.d(TAG, "onError: code=$code; message=$message")
    }
})
yoTestCaptchaVerify.verify()
```

#### YoTestListener.onClose(data)
- `data` \<String?\> 可以为空
- `return`: Unit

验证关闭的回调监听

```kotlin
val yoTestCaptchaVerify = YoTestCaptchaVerify(this, object : YoTestListener(){
    override fun onClose(data: String?) {
        Log.d(TAG, "onClose: $data")
    }
})
yoTestCaptchaVerify.verify()
```