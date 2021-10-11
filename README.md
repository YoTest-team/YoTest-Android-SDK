YoTest-Android-SDK 文档
----
> 基于虚拟机保护、设备特征识别和操作行为识别的新一代智能验证码，具备智能评分、抗Headless、模拟伪装、针对恶意设备自动提升验证难度等多项安全措施，帮助开发者减少恶意攻击导致的数字资产损失，强力护航业务安全。

* [仓库入口](https://github.com/YoTest-team/YoTest-Android-SDK#%E4%BB%93%E5%BA%93%E5%85%A5%E5%8F%A3)
* [兼容性](https://github.com/YoTest-team/YoTest-Android-SDK#%E5%85%BC%E5%AE%B9%E6%80%A7)
* [安装](https://github.com/YoTest-team/YoTest-Android-SDK#%E5%AE%89%E8%A3%85)
* [快速开始](https://github.com/YoTest-team/YoTest-Android-SDK#%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B)
* [验证模式](https://github.com/YoTest-team/YoTest-Android-SDK#%E9%AA%8C%E8%AF%81%E6%A8%A1%E5%BC%8F)
* [API](https://github.com/YoTest-team/YoTest-Android-SDK#api)


### 仓库入口：
<a href="https://github.com/YoTest-team/YoTest-Android-SDK"><img src="./images/GitHub.png" width="32px"/></a>

### 兼容性
> 以下兼容性根据[BrowserStack](https://live.browserstack.com/)的相关真机测试得出，仅供参考

* Android 4.4+ （即API19及以上）

### 安装

在工程根目录的build.gradle中添加

```groovy
buildscript {
    repositories {
        mavenCentral()
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
  implementation 'com.fastyotest:captcha:x.x.x'
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

### 验证模式

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

// 支持点击蒙层取消验证，建议加上以下方法，实现点击物理返回键/滑动返回取消验证，而不是返回上一页
override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_BACK && yoTestCaptchaVerify.isShow()) {
        yoTestCaptchaVerify.cancel()
        return true
    }
    return super.onKeyDown(keyCode, event)
}

```


### API

初始化函数

* [init(context, accessId, callback)](https://github.com/YoTest-team/YoTest-Android-SDK#initcontext-accessId-callback)

YoTestCaptchaVerify实例方法
需要先初始化

* [YoTestCaptchaVerify(context, listener)](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestCaptchaVerifycontext-listener)
* [verify()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestCaptchaVerifyverify)
* [isShow()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestCaptchaVerifyverify)
* [cancel()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestCaptchaVerifyverify)
* [onDestory()](https://github.com/YoTest-team/YoTest-Android-SDK#YoTestCaptchaVerifyverify)

#### init(context, accessId, callback)
  - `context` \<Context\> 必填，ApplicationContext
  - `accessId ` \<String\> 必填，当前项目所属的accessId，可以在友验后台中进行相关获取及查看
  - `callback` \<Function\>
    - **code**: \<Int\> 错误码，其返回非0时，请做好错误处理
    - **message**: \<String\> 提示信息
  - `return:` Unit


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
