package net.gotev.cookiestore.okhttp

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.io.IOException
import java.net.CookieHandler
import java.net.HttpCookie
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A cookie jar that delegates to a [java.net.CookieHandler].
 *
 * Copied from https://github.com/square/okhttp/blob/master/okhttp-urlconnection/src/main/java/okhttp3/JavaNetCookieJar.kt
 * to not depend on okhttp-urlconnection as suggested by @swankjesse
 */
class JavaNetCookieJar(private val cookieHandler: CookieHandler) : CookieJar {

    companion object {
        private const val WARN = 5
    }

    private val logger by lazy {
        Logger.getLogger(javaClass.name)
    }

    private val STANDARD_DATE_FORMAT = object : ThreadLocal<DateFormat>() {
        override fun initialValue(): DateFormat {
            // Date format specified by RFC 7231 section 7.1.1.1.
            return SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("GMT")
            }
        }
    }

    private fun log(level: Int, message: String, t: Throwable?) {
        val logLevel = if (level == WARN) Level.WARNING else Level.INFO
        logger.log(logLevel, message, t)
    }

    private fun Date.toHttpDateString(): String = STANDARD_DATE_FORMAT.get().format(this)

    /**
     * @param forObsoleteRfc2965 true to include a leading `.` on the domain pattern. This is
     *     necessary for `example.com` to match `www.example.com` under RFC 2965. This extra dot is
     *     ignored by more recent specifications.
     */
    private fun Cookie.toString(forObsoleteRfc2965: Boolean): String {
        return buildString {
            append(name)
            append('=')
            append(value)

            if (persistent) {
                if (expiresAt == Long.MIN_VALUE) {
                    append("; max-age=0")
                } else {
                    append("; expires=").append(Date(expiresAt).toHttpDateString())
                }
            }

            if (!hostOnly) {
                append("; domain=")
                if (forObsoleteRfc2965) {
                    append(".")
                }
                append(domain)
            }

            append("; path=").append(path)

            if (secure) {
                append("; secure")
            }

            if (httpOnly) {
                append("; httponly")
            }

            return toString()
        }
    }

    /**
     * Returns the index of the first character in this string that contains a character in
     * [delimiters]. Returns endIndex if there is no such character.
     */
    private fun String.delimiterOffset(
        delimiters: String,
        startIndex: Int = 0,
        endIndex: Int = length
    ): Int {
        for (i in startIndex until endIndex) {
            if (this[i] in delimiters) return i
        }
        return endIndex
    }

    /**
     * Increments [startIndex] until this string is not ASCII whitespace. Stops at [endIndex].
     */
    private fun String.indexOfFirstNonAsciiWhitespace(
        startIndex: Int = 0,
        endIndex: Int = length
    ): Int {
        for (i in startIndex until endIndex) {
            when (this[i]) {
                '\t', '\n', '\u000C', '\r', ' ' -> Unit
                else -> return i
            }
        }
        return endIndex
    }

    /**
     * Decrements [endIndex] until `input[endIndex - 1]` is not ASCII whitespace. Stops at [startIndex].
     */
    private fun String.indexOfLastNonAsciiWhitespace(
        startIndex: Int = 0,
        endIndex: Int = length
    ): Int {
        for (i in endIndex - 1 downTo startIndex) {
            when (this[i]) {
                '\t', '\n', '\u000C', '\r', ' ' -> Unit
                else -> return i + 1
            }
        }
        return startIndex
    }

    /** Equivalent to `string.substring(startIndex, endIndex).trim()`. */
    private fun String.trimSubstring(startIndex: Int = 0, endIndex: Int = length): String {
        val start = indexOfFirstNonAsciiWhitespace(startIndex, endIndex)
        val end = indexOfLastNonAsciiWhitespace(start, endIndex)
        return substring(start, end)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookieStrings = mutableListOf<String>()
        for (cookie in cookies) {
            cookieStrings.add(cookie.toString(forObsoleteRfc2965 = true))
        }
        val multimap = mapOf("Set-Cookie" to cookieStrings)
        try {
            cookieHandler.put(url.toUri(), multimap)
        } catch (e: IOException) {
            log(WARN, "Saving cookies failed for " + url.resolve("/...")!!, e)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieHeaders = try {
            // The RI passes all headers. We don't have 'em, so we don't pass 'em!
            cookieHandler.get(url.toUri(), emptyMap<String, List<String>>())
        } catch (e: IOException) {
            log(WARN, "Loading cookies failed for " + url.resolve("/...")!!, e)
            return emptyList()
        }

        var cookies: MutableList<Cookie>? = null
        for ((key, value) in cookieHeaders) {
            if (("Cookie".equals(key, ignoreCase = true) || "Cookie2".equals(
                    key,
                    ignoreCase = true
                )) &&
                value.isNotEmpty()
            ) {
                for (header in value) {
                    if (cookies == null) cookies = mutableListOf()
                    cookies.addAll(decodeHeaderAsJavaNetCookies(url, header))
                }
            }
        }

        return if (cookies != null) {
            Collections.unmodifiableList(cookies)
        } else {
            emptyList()
        }
    }

    /**
     * Convert a request header to OkHttp's cookies via [HttpCookie]. That extra step handles
     * multiple cookies in a single request header, which [Cookie.parse] doesn't support.
     */
    private fun decodeHeaderAsJavaNetCookies(url: HttpUrl, header: String): List<Cookie> {
        val result = mutableListOf<Cookie>()
        var pos = 0
        val limit = header.length
        var pairEnd: Int
        while (pos < limit) {
            pairEnd = header.delimiterOffset(";,", pos, limit)
            val equalsSign = header.delimiterOffset("=", pos, pairEnd)
            val name = header.trimSubstring(pos, equalsSign)
            if (name.startsWith("$")) {
                pos = pairEnd + 1
                continue
            }

            // We have either name=value or just a name.
            var value = if (equalsSign < pairEnd) {
                header.trimSubstring(equalsSign + 1, pairEnd)
            } else {
                ""
            }

            // If the value is "quoted", drop the quotes.
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length - 1)
            }

            result.add(
                Cookie.Builder()
                    .name(name)
                    .value(value)
                    .domain(url.host)
                    .build()
            )
            pos = pairEnd + 1
        }
        return result
    }
}
