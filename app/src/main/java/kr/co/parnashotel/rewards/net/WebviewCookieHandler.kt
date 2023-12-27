package kr.co.parnashotel.rewards.net

import android.webkit.CookieManager
import java.io.IOException
import java.net.CookieHandler
import java.net.URI
import java.util.*

class WebviewCookieHandler : CookieHandler() {
    val webviewCookieManager = CookieManager.getInstance()

    @Throws(IOException::class)
    override fun get(uri: URI, requestHeaders: Map<String, List<String>>): Map<String, List<String>> {
        val url = uri.toString()
        val cookieValue: String = this.webviewCookieManager.getCookie(url)
        val cookies: MutableMap<String, List<String>> = HashMap()
        cookies["Cookie"] = listOf(cookieValue)
        return cookies
    }

    @Throws(IOException::class)
    override fun put(uri: URI, responseHeaders: Map<String, List<String>>) {
        val url = uri.toString()
        for (header in responseHeaders.keys) {
            if (header.equals("Set-Cookie", ignoreCase = true) || header.equals(
                    "Set-Cookie2",
                    ignoreCase = true
                )
            ) {
                for (value in responseHeaders[header]!!) {
                    this.webviewCookieManager.setCookie(url, value)
                }
            }
        }
    }
}