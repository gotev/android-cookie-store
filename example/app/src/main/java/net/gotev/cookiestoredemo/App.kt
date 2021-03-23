package net.gotev.cookiestoredemo

import android.app.Application
import com.ashokvarma.gander.Gander
import com.ashokvarma.gander.GanderInterceptor
import com.ashokvarma.gander.imdb.GanderIMDB
import net.gotev.cookiestore.InMemoryCookieStore
import net.gotev.cookiestore.SharedPreferencesCookieStore
import net.gotev.cookiestore.WebKitSyncCookieManager
import net.gotev.cookiestore.okhttp.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
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

        lateinit var cookieManager: WebKitSyncCookieManager
        lateinit var cookieAPI: CookieAPI
    }

    private fun createCookieStore(name: String, persistent: Boolean) = if (persistent) {
        SharedPreferencesCookieStore(this, name)
    } else {
        InMemoryCookieStore(name)
    }

    override fun onCreate() {
        super.onCreate()

        cookieManager = WebKitSyncCookieManager(
            createCookieStore(name = cookieStoreName, persistent = true),
            CookiePolicy.ACCEPT_ALL
        )

        // Setup for HttpURLConnection
        CookieManager.setDefault(cookieManager)

        Gander.setGanderStorage(GanderIMDB.getInstance())

        // Setup for OkHttp
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addNetworkInterceptor(GanderInterceptor(this).showNotification(true))
            .build()

        cookieAPI = Retrofit.Builder()
            .baseUrl(baseAPIUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CookieAPI::class.java)
    }
}
