package net.gotev.cookiestore

import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.net.URI

class WebKitSyncCookieManager(
    store: CookieStore,
    cookiePolicy: CookiePolicy
) : CookieManager(store, cookiePolicy) {

    private val webKitCookieManager by lazy {
        android.webkit.CookieManager.getInstance()
    }

    init {
        webKitCookieManager.setAcceptCookie(true)
    }

    override fun put(uri: URI?, responseHeaders: MutableMap<String, MutableList<String>>?) {
        super.put(uri, responseHeaders)
        cookieStore.syncToWebKitCookieManager()
    }

    fun removeAll() {
        cookieStore.removeAll()
        webKitCookieManager.removeAll()
    }
}
