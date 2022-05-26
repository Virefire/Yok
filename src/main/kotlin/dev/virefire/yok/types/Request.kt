package dev.virefire.yok.types

import dev.virefire.kson.KSON
import dev.virefire.yok.Yok
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.Proxy
import java.net.URLEncoder
import java.nio.charset.Charset

class Request internal constructor(internal val client: Yok) {
    interface Body {
        var raw: ByteArray?
        var json: Any?
        var form: Map<String, String>?
        var text: String?
        var multipart: MultipartBuilder?
        var stream: InputStream?
    }
    var url: String? = null
    var method: Method? = null
    var headers: Headers = headersOf()
    var params: Map<String, String> = emptyMap()
    var query: Map<String, String> = emptyMap()
    internal val baseHeaders: Headers = headersOf()
    var proxy: Proxy? = null
    var connectTimeout: Int? = null
    var readTimeout: Int? = null
    var followRedirects: Boolean? = null
    var addDefaultUserAgent: Boolean? = null
    internal var bodyStream: InputStream? = null
    val body = object: Body {
        override var raw: ByteArray? = null
            set(value) {
                bodyStream = ByteArrayInputStream(value!!)
                field = value
            }
        override var json: Any? = null
            set(value) {
                bodyStream = ByteArrayInputStream(KSON.stringify(value).toByteArray())
                baseHeaders += "Content-Type" to "application/json"
                field = value
            }
        override var form: Map<String, String>? = null
            set(value) {
                bodyStream = ByteArrayInputStream(
                    value!!
                        .map { "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}" }
                        .joinToString("&")
                        .toByteArray()
                )
                baseHeaders += "Content-Type" to "application/x-www-form-urlencoded"
                field = value
            }
        override var text: String? = null
            set(value) {
                bodyStream = ByteArrayInputStream(value!!.toByteArray())
                baseHeaders += "Content-Type" to "text/plain"
                field = value
            }
        override var multipart: MultipartBuilder? = null
            set(value) {
                val entity = value!!.build()
                bodyStream = entity.content
                baseHeaders += "Content-Type" to entity.contentType.value
                field = value
            }
        override var stream: InputStream? = null
            set(value) {
                bodyStream = value
                field = value
            }
    }
    internal val interceptors: MutableList<Interceptor> = mutableListOf()
    fun interceptor(interceptor: Interceptor) {
        interceptors.add(interceptor)
    }

    fun inherit(request: Request) {
        url = request.url
        method = request.method
        headers = request.headers
        params = request.params
        query = request.query
        proxy = request.proxy
        connectTimeout = request.connectTimeout
        readTimeout = request.readTimeout
        followRedirects = request.followRedirects
        addDefaultUserAgent = request.addDefaultUserAgent
        interceptors.addAll(request.interceptors)
        if (body.raw != null) body.raw = request.body.raw
        if (body.json != null) body.json = request.body.json
        if (body.form != null) body.form = request.body.form
        if (body.text != null) body.text = request.body.text
        if (body.multipart != null) body.multipart = request.body.multipart
        if (body.stream != null) body.stream = request.body.stream
    }
}