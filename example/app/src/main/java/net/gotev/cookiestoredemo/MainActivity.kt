package net.gotev.cookiestoredemo

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * This is only to show the cookie store functionality with a WebView in the simplest way
 * possible to make you understand it. Don't take this as a production example!
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // login send back a cookie
        login.setOnClickListener {
            App.cookieAPI.login(LoginPayload(username = App.username))
                .enqueue(object : Callback<Unit> {
                    override fun onFailure(call: Call<Unit>, error: Throwable) {
                        toast("Login KO: $error")
                    }

                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        toast("Login OK")
                        reloadUrl()
                    }
                })
        }

        // home call sends the cookie back
        login_status.setOnClickListener {
            App.cookieAPI.home()
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        toast(response.body().orEmpty())
                    }

                    override fun onFailure(call: Call<String>, error: Throwable) {
                        toast("Login status KO: $error")
                    }
                })
        }

        // this clears all the cookies
        clearCookies.setOnClickListener {
            App.cookieManager.removeAll()
            toast("Cookies Cleared!")
            reloadUrl()
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val webKitCookieManager = android.webkit.CookieManager.getInstance()
                val cookie = webKitCookieManager.getCookie(url)
                Log.e("COOKIE", "For url $url: [$cookie]")
            }
        }

        reloadUrl()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // load home in webview to check cookie sync
    private fun reloadUrl() {
        webView.loadUrl(App.webViewUrl)
    }
}
