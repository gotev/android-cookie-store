package net.gotev.cookiestore

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpCookie
import java.net.URI

open class SharedPreferencesCookieStore(
    context: Context,
    private val name: String
) : InMemoryCookieStore(name) {

    private val preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        synchronized(SharedPreferencesCookieStore::class.java) {
            preferences.all.forEach { (key, value) ->
                try {
                    val index = URI.create(key)
                    val listType = object : TypeToken<ArrayList<HttpCookie>>() {}.type

                    val cookies: ArrayList<HttpCookie> = gson.fromJson(value.toString(), listType)

                    uriIndex[index] = cookies
                } catch (exception: Throwable) {
                    Log.e(
                        javaClass.simpleName,
                        "Error while loading key = $key, value = $value from cookie store named $name",
                        exception
                    )
                }
            }
        }
    }

    override fun removeAll(): Boolean = synchronized(SharedPreferencesCookieStore::class.java) {
        super.removeAll()
        preferences.edit().clear().commit()
        true
    }

    override fun add(uri: URI?, cookie: HttpCookie?) {
        synchronized(SharedPreferencesCookieStore::class.java) {
            super.add(uri, cookie)

            uri?.let {
                val index = getEffectiveURI(uri)
                uriIndex[index]?.let { cookies ->
                    preferences
                        .edit()
                        .putString(index.toString(), gson.toJson(ArrayList(cookies)))
                        .commit()
                }
            }
        }
    }

    override fun remove(uri: URI?, cookie: HttpCookie?): Boolean =
        synchronized(SharedPreferencesCookieStore::class.java) {
            val result = super.remove(uri, cookie)

            uri?.let {
                val index = getEffectiveURI(uri)
                val cookies = uriIndex[index]

                preferences.edit().apply {
                    if (cookies == null) {
                        remove(index.toString())
                    } else {
                        putString(index.toString(), gson.toJson(ArrayList(cookies)))
                    }
                }.commit()
            }

            result
        }
}
