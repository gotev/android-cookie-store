package net.gotev.cookiestoredemo

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * This is only to show the cookie store functionality with a WebView in the simplest way
 * possible to make you understand it. Don't take this as a production example!
 */
class MainActivity : AppCompatActivity() {
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // login sends back a cookie
        login.setOnClickListener {
            scope.launch {
                try {
                    App.cookieAPI.login(LoginPayload(username = App.username))
                    toast("Login OK")
                    reloadUrl()
                } catch (exc: Throwable) {
                    toast("Login KO: $exc")
                }
            }
        }

        // home call sends the cookie back to the server if present
        login_status.setOnClickListener {
            scope.launch {
                try {
                    val message = App.cookieAPI.home()
                    toast(message)
                } catch (exc: Throwable) {
                    toast("Login status KO: $exc")
                }
            }
        }

        // this clears all the cookies
        clearCookies.setOnClickListener {
            App.cookieManager.removeAll()
            toast("Cookies Cleared!")
            reloadUrl()
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                try {
                    val cookie = android.webkit.CookieManager.getInstance().getCookie(url)
                    Log.e("COOKIE", "For url $url: [$cookie]")
                    toast("Cookie for url $url: [$cookie]")
                } catch (exc: Throwable) {
                    Log.e("COOKIE-MANAGER", "Android WebKitCookieManager error", exc)
                    toast("$exc")
                }
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
