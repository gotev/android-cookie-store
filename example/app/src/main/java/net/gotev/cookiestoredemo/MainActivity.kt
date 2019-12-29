package net.gotev.cookiestoredemo

import android.os.Bundle
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

        clearCookies.setOnClickListener {
            App.cookieManager.removeAll()
            toast("Cookies Cleared!")
            reloadUrl()
        }

        reloadUrl()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun reloadUrl() {
        webView.loadUrl(App.webViewUrl)
    }
}
