# Android Cookie Store
Android InMemory and persistent Cookie Store for `HttpURLConnection` and `OkHttp`.

## Why?
Neither `HttpURLConnection` nor `OkHttp` provides a native and rapid way of storing cookies on Android. This library aims to fill this gap, by extending standard `java.net.CookieStore` in Kotlin, with extendability in mind.

With this library you have:
- an in memory only cookie store
- a shared preferences backed cookie store which can survive app reboots
- possibility to extend both to provide your own custom implementation which best fits your needs without reinventing the wheel for cookie management

## Getting started
Add this to your dependencies:

```groovy
implementation 'net.gotev:cookie-store:1.0.0'
```
