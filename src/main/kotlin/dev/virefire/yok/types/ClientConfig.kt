package dev.virefire.yok.types

import java.net.Proxy

class ClientConfig {
    var baseUrl: String? = null
    var headers: Headers = headersOf()
    var proxy: Proxy? = null
    var connectTimeout = 10000
    var readTimeout = 10000
    var followRedirects = true
    var addDefaultUserAgent = true
    internal val interceptors: MutableList<Interceptor> = mutableListOf()
    fun interceptor(interceptor: Interceptor) {
        interceptors.add(interceptor)
    }
}