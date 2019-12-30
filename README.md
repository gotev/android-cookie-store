# Android Cookie Store 
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Cookie%20Store-green.svg?style=flat)](https://android-arsenal.com/details/1/8000) [![Android Weekly](https://img.shields.io/badge/Android%20Weekly-394-green)](https://androidweekly.net/issues/issue-394) [![Build Status](https://travis-ci.org/gotev/android-cookie-store.svg?branch=master)](https://travis-ci.org/gotev/android-cookie-store) [![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/) [ ![Download](https://api.bintray.com/packages/gotev/maven/android-cookie-store/images/download.svg) ](https://bintray.com/gotev/maven/android-cookie-store/_latestVersion) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com) [![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=alexgotev%40gmail%2ecom&lc=US&item_name=Android%20Upload%20Service&item_number=AndroidUploadService&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted)

Android InMemory and persistent Cookie Store for `HttpURLConnection` and `OkHttp`, with extensions to easily sync cookies in Android WebViews.

## Why?
Neither `HttpURLConnection` nor `OkHttp` provides a native and rapid way of storing cookies persistently on Android. This library aims to fill this gap, by implementing the standard `java.net.InMemoryCookieStore` in Kotlin, with extendability in mind.

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
implementation "net.gotev:cookie-store:1.2.1"
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
Add the following dependency (suitable for JVM and Android):

```groovy
implementation "net.gotev:cookie-store-okhttp:$cookieStoreVersion"
```

And when you build your OkHttpClient, set the Cookie Jar:

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .cookieJar(JavaNetCookieJar(cookieManager))
    .build()
```

### WebView
It's a common thing to obtain a cookie from an API and to open an authenticated web page inside an app which needs the cookie. You can find a complete working example in the demo app.

You have two ways of doing this:
- Using `WebKitSyncCookieManager`
- Using standard `java.net.CookieManager`

#### Using WebKitSyncCookieManager
```kotlin
val cookieManager = WebKitSyncCookieManager(
    createCookieStore(name = "myCookies", persistent = true),
    CookiePolicy.ACCEPT_ALL
)
```
Then follow standard instructions from the Usage section to setup `HttpURLConnection` or `OkHttp` according to your needs.

> Incoming Cookies will be automatically synced to WebKit's CookieManager. Syncing is unidirectional from `WebKitSyncCookieManager` to `android.webkit.CookieManager` to have a single source of truth and to prevent attacks coming from URLs loaded in WebViews. If you need bi-directional sync, think twice before doing it.

To clear cookies:

```kotlin
cookieManager.removeAll()
```

This will clear both the `CookieStore` and WebKit's Cookie Manager.

#### Using standard java.net.CookieManager
Cookies syncing is entirely up to you and manual.

To copy all cookies from the cookie store to the WebKit Cookie Manager:
```kotlin
cookieManager.cookieStore.syncToWebKitCookieManager()
```
Remember to do this before loading any URL in your web view.

To remove all cookies from the Cookie Store:
```kotlin
cookieManager.cookieStore.removeAll()
```

To remove all cookies from WebKit Cookie Manager:
```kotlin
android.webkit.CookieManager.getInstance().removeAll()
```

That's all folks!
