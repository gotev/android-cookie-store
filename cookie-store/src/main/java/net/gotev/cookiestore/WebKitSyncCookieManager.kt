package net.gotev.cookiestore

import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.net.URI

class WebKitSyncCookieManager(
    store: CookieStore,
    cookiePolicy: CookiePolicy,
    private val onWebKitCookieManagerError: ((Throwable) -> Unit)? = null
) : CookieManager(store, cookiePolicy) {

    private val webKitCookieManager by lazy {
        android.webkit.CookieManager.getInstance()
    }

    init {
        try {
            webKitCookieManager.setAcceptCookie(true)
        } catch (exc: Throwable) {
            onWebKitCookieManagerError?.invoke(exc)
        }
    }

    override fun put(uri: URI?, responseHeaders: MutableMap<String, MutableList<String>>?) {
        super.put(uri, responseHeaders)
        try {
            cookieStore.syncToWebKitCookieManager(webKitCookieManager)
        } catch (exc: Throwable) {
            onWebKitCookieManagerError?.invoke(exc)
        }
    }

    fun removeAll() {
        cookieStore.removeAll()
        try {
            webKitCookieManager.removeAll()
        } catch (exc: Throwable) {
            onWebKitCookieManagerError?.invoke(exc)
        }
    }
}
