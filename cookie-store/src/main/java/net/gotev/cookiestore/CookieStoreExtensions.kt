package net.gotev.cookiestore

import android.os.Build
import java.net.CookieStore

@Synchronized
fun CookieStore.syncToWebKitCookieManager() {
    val webKitCookieManager = android.webkit.CookieManager.getInstance()

    cookies.forEach {
        val hostUrl = "${if (it.secure) "https" else "http"}://${it.domain}"
        webKitCookieManager.setCookie(hostUrl, it.toString())
    }

    if (Build.VERSION.SDK_INT >= 21) {
        webKitCookieManager.flush()
    }
}

@SuppressWarnings("DEPRECATION")
@Synchronized
fun android.webkit.CookieManager.removeAll() {
    if (Build.VERSION.SDK_INT >= 21) {
        removeAllCookies(null)
        flush()
    } else {
        removeAllCookie()
    }
}
