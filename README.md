# Android Cookie Store [![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/) [ ![Download](https://api.bintray.com/packages/gotev/maven/android-cookie-store/images/download.svg) ](https://bintray.com/gotev/maven/android-upload-service/_latestVersion) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com) [![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=alexgotev%40gmail%2ecom&lc=US&item_name=Android%20Upload%20Service&item_number=AndroidUploadService&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted)
Android InMemory and persistent Cookie Store for `HttpURLConnection` and `OkHttp`.

## Why?
Neither `HttpURLConnection` nor `OkHttp` provides a native and rapid way of storing cookies persistently on Android. This library aims to fill this gap, by extending standard `java.net.CookieStore` in Kotlin, with extendability in mind.

With this library you have:
- super tiny footprint (the library is only a bunch of classes)
- an in memory only cookie store
- a shared preferences backed cookie store which can survive app reboots
- possibility to extend both to provide your own custom implementation which best fits your needs without reinventing the wheel for cookie management

## Compatibility
Android API 16+

## Getting started
Add this to your dependencies:

```groovy
implementation 'net.gotev:cookie-store:1.0.0'
```

## Usage
Create your Cookie Manager:

```kotlin
// Example extension function to demonstrate how to create both cookie stores
fun Context.createCookieStore(name: String, persistent: Boolean) = if (persistent) {
    SharedPreferencesCookieStore(applicationContext, name)
} else {
    InMemoryCookieStore(name)
}

val cookieManager = CookieManager(
    createCookieStore(name = "myCookies", persistent = true),
    CookiePolicy.ACCEPT_ALL
)
```

### HttpURLConnection
To setup the default Cookie Manager:

```kotlin
CookieManager.setDefault(cookieManager)
```

### OkHttp
Add the following dependency:

```groovy
implementation "com.squareup.okhttp3:okhttp-urlconnection:$okHttpVersion"
```

And when you build your OkHttpClient, set the Cookie Jar:

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .cookieJar(JavaNetCookieJar(cookieManager))
    .build()
```

That's all folks!
