package net.gotev.cookiestoredemo

import android.app.Application
import net.gotev.cookiestore.InMemoryCookieStore
import net.gotev.cookiestore.SharedPreferencesCookieStore
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy

/**
 * @author Aleksandar Gotev
 */
class App : Application() {

    companion object {
        const val baseAPIUrl = "https://www.gotev.net/cookiedemo/"
        const val webViewUrl = "https://www.gotev.net/cookiedemo/home.php"
        const val cookieStoreName = "myCookies"
        const val username = "userdemo"

        lateinit var cookieManager: CookieManager
        lateinit var cookieAPI: CookieAPI
    }

    private fun createCookieStore(name: String, persistent: Boolean) = if (persistent) {
        SharedPreferencesCookieStore(this, name)
    } else {
        InMemoryCookieStore(name)
    }

    override fun onCreate() {
        super.onCreate()

        cookieManager = CookieManager(
            createCookieStore(name = cookieStoreName, persistent = true),
            CookiePolicy.ACCEPT_ALL
        )

        // Setup for HttpURLConnection
        CookieManager.setDefault(cookieManager)

        // Setup for OkHttp
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()

        cookieAPI = Retrofit.Builder()
            .baseUrl(baseAPIUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CookieAPI::class.java)
    }
}
