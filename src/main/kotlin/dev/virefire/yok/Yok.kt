package dev.virefire.yok

import dev.virefire.yok.types.ClientConfig
import dev.virefire.yok.types.Request
import dev.virefire.yok.types.Response
import java.util.*

open class Yok constructor() {
    companion object : Yok() {
        internal val version: String
        init {
            val p = Properties()
            val stream = Yok::class.java.getResourceAsStream("/version.properties")
            version = if (stream != null) {
                p.load(stream)
                p.getProperty("version")
            } else {
                "[unknown]"
            }
        }
    }

    internal val config = ClientConfig()

    constructor(configurator: ClientConfig.() -> Unit) : this() {
        config.configurator()
    }

    fun request(cfg: Request.() -> Unit): Response {
        val request = Request(this)
        request.cfg()
        return performRequest(request)
    }

    fun get(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.GET
            this.url = url
            cfg?.invoke(this)
        }
    }

    fun post(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.POST
            this.url = url
            cfg?.invoke(this)
        }
    }

    fun put(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.PUT
            this.url = url
            cfg?.invoke(this)
        }
    }

    fun delete(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.DELETE
            this.url = url
            cfg?.invoke(this)
        }
    }

    fun patch(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.PATCH
            this.url = url
            cfg?.invoke(this)
        }
    }

    fun head(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.HEAD
            this.url = url
            cfg?.invoke(this)
        }
    }

    fun options(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.OPTIONS
            this.url = url
            cfg?.invoke(this)
        }
    }

    fun trace(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.TRACE
            this.url = url
            cfg?.invoke(this)
        }
    }

    fun connect(url: String, cfg: (Request.() -> Unit)? = null): Response {
        return request {
            this.method = dev.virefire.yok.types.Method.CONNECT
            this.url = url
            cfg?.invoke(this)
        }
    }
}