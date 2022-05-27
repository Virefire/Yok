package dev.virefire.yok

import dev.virefire.yok.types.Headers
import dev.virefire.yok.types.Request
import dev.virefire.yok.types.Response
import dev.virefire.yok.types.headersOf
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

internal fun performRequest(req: Request): Response {
    if (req.url == null) throw IllegalArgumentException("URL must be specified")
    if (req.method == null) throw IllegalArgumentException("Method must be specified")
    if (req.url!!.startsWith('/')) {
        if (req.client.config.baseUrl == null) throw IllegalArgumentException("Base URL must be specified")
        req.url = req.client.config.baseUrl + req.url
    }
    val interceptors = req.interceptors + req.client.config.interceptors
    var i = 0
    fun process(): Response {
        var httpResponse: Response? = null
        return if (i < interceptors.size) {
            val interceptor = interceptors[i]
            i++
            interceptor.intercept(req, client = req.client, next = { process() })
        } else {
            if (httpResponse == null) {
                httpResponse = httpRequest(req)
                httpResponse
            } else {
                httpResponse
            }
        }
    }
    return process()
}

private fun httpRequest(req: Request): Response {
    val urlString = req.url!!.split("/").map {
        if (it.startsWith(':') && it.length > 1) {
            return@map URLEncoder.encode(req.params[it.substring(1)]?: "", "UTF-8")
        } else {
            return@map it
        }
    }.joinToString("/")
    val queryString = req.query.map {
        "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
    }.joinToString("&")

    val url = URL(urlString + if (queryString.isNotEmpty()) (if (urlString.contains('?')) "&$queryString" else "?$queryString") else "")
    val proxy = req.proxy ?: req.client.config.proxy

    val connection: HttpURLConnection =
        if (proxy != null)
            url.openConnection(proxy) as HttpURLConnection
        else
            url.openConnection() as HttpURLConnection

    setMethod(connection, req.method!!)
    connection.connectTimeout = req.connectTimeout ?: req.client.config.connectTimeout
    connection.readTimeout = req.readTimeout ?: req.client.config.readTimeout
    connection.instanceFollowRedirects = req.followRedirects ?: req.client.config.followRedirects
    connection.doInput = true

    val headers = headersOf()
    headers.addAll(req.baseHeaders.getAll())
    headers.addAllRewriteDuplicates(req.client.config.headers)
    headers.addAllRewriteDuplicates(req.headers)

    val addDefaultUserAgent = req.addDefaultUserAgent ?: req.client.config.addDefaultUserAgent
    if (addDefaultUserAgent && !headers.containsKey("User-Agent")) {
        headers["User-Agent"] = "Yok/" + Yok.version
    }

    headers.getAll().forEach {
        connection.addRequestProperty(it.first, it.second)
    }

    if (req.bodyStream != null) {
        connection.doOutput = true
        connection.outputStream.use {
            req.bodyStream!!.copyTo(it)
        }
    }

    val status = connection.responseCode

    val resHeaders = headersOf()

    connection.headerFields.forEach {
        val key = it.key ?: return@forEach
        it.value.forEach { value ->
            resHeaders.add(key, value)
        }
    }

    return Response(status, resHeaders, if (status >= 300) connection.errorStream else connection.inputStream)
}

private fun Headers.addAllRewriteDuplicates(other: Headers) {
    val thisList = this.getAll()
    val otherList = other.getAll()
    otherList.forEach {
        while (true) {
            val pos = thisList.indexOfFirst { el -> el.first.equals(it.first, true) }
            if (pos >= 0) {
                if (otherList.contains(thisList[pos])) break
                thisList.removeAt(pos)
            } else {
                thisList.add(it)
                break
            }
        }
    }
}
