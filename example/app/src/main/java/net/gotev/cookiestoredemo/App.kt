package net.gotev.cookiestoredemo

import android.app.Application
import net.gotev.cookiestore.InMemoryCookieStore
import net.gotev.cookiestore.SharedPreferencesCookieStore
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy

/**
 * @author Aleksandar Gotev
 */
class App : Application() {

    private fun createCookieStore(name: String, persistent: Boolean) = if (persistent) {
        SharedPreferencesCookieStore(this, name)
    } else {
        InMemoryCookieStore(name)
    }

    override fun onCreate() {
        super.onCreate()

        val cookieManager = CookieManager(
            createCookieStore(name = "myCookies", persistent = true),
            CookiePolicy.ACCEPT_ALL
        )

        //Setup for HttpURLConnection
        CookieManager.setDefault(cookieManager)

        //Setup for OkHttp
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }
}
