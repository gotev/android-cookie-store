package net.gotev.cookiestore

import android.util.Log
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.net.URI

class WebKitSyncCookieManager(
    store: CookieStore,
    cookiePolicy: CookiePolicy,
    private val onWebKitCookieManagerError: ((Throwable) -> Unit)? = null
) : CookieManager(store, cookiePolicy) {

    private inline fun handleExceptions(block: () -> Unit) {
        try {
            block()
        } catch (exc: Throwable) {
            onWebKitCookieManagerError?.invoke(exc)
                ?: Log.e(
                    "COOKIE-STORE",
                    "Unhandled WebKitSyncCookieManager error. " +
                        "You could handle it by setting onWebKitCookieManagerError when you create " +
                        "WebKitSyncCookieManager. This exception is caused by the underlying " +
                        "android.webkit.CookieManager", exc
                )
        }
    }

    init {
        handleExceptions {
            android.webkit.CookieManager.getInstance().setAcceptCookie(true)
        }
    }

    override fun put(uri: URI?, responseHeaders: MutableMap<String, MutableList<String>>?) {
        super.put(uri, responseHeaders)
        handleExceptions {
            cookieStore.syncToWebKitCookieManager()
        }
    }

    fun removeAll() {
        cookieStore.removeAll()
        handleExceptions {
            android.webkit.CookieManager.getInstance().removeAll()
        }
    }
}
