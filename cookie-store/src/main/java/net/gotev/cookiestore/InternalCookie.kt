package net.gotev.cookiestore

import android.os.Build
import java.net.HttpCookie

data class InternalCookie(
    val comment: String?,
    val commentURL: String?,
    val discard: Boolean?,
    val domain: String,
    val maxAge: Long?,
    val name: String,
    val path: String?,
    val portlist: String?,
    val secure: Boolean?,
    val value: String,
    val version: Int?,
    var httpOnly: Boolean? = null
) {
    constructor(cookie: HttpCookie) : this(
        comment = cookie.comment,
        commentURL = cookie.commentURL,
        discard = cookie.discard,
        domain = cookie.domain,
        maxAge = cookie.maxAge,
        name = cookie.name,
        path = cookie.path,
        portlist = cookie.portlist,
        secure = cookie.secure,
        value = cookie.value,
        version = cookie.version
    )

    fun toHttpCookie() = HttpCookie(name, value).apply {
        comment = this@InternalCookie.comment
        commentURL = this@InternalCookie.commentURL
        discard = this@InternalCookie.discard == true
        domain = this@InternalCookie.domain
        maxAge = this@InternalCookie.maxAge ?: 0
        path = this@InternalCookie.path
        portlist = this@InternalCookie.portlist
        secure = this@InternalCookie.secure == true
        version = this@InternalCookie.version ?: 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isHttpOnly = this@InternalCookie.httpOnly == true
        }
    }
}
