# Android Cookie Store
Android InMemory and persistent Cookie Store for `HttpURLConnection` and `OkHttp`.

## Why?
Neither `HttpURLConnection` nor `OkHttp` provides a native and rapid way of storing cookies persistently on Android. This library aims to fill this gap, by extending standard `java.net.CookieStore` in Kotlin, with extendability in mind.

With this library you have:
- an in memory only cookie store
- a shared preferences backed cookie store which can survive app reboots
- possibility to extend both to provide your own custom implementation which best fits your needs without reinventing the wheel for cookie management

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
