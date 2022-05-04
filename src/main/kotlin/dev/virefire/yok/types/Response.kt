package dev.virefire.yok.types

import dev.virefire.kson.KSON
import dev.virefire.kson.ParsedElement
import java.io.InputStream

class Response internal constructor(val status: Int, val headers: Headers, private val _body: InputStream) {
    interface Body {
        val raw: ByteArray
        val json: ParsedElement
        val text: String
        val stream: InputStream
    }
    private enum class BodyTypeLock {
        NONE, RAW, JSON, TEXT, STREAM
    }

    private var bodyTypeLock = BodyTypeLock.NONE
    private var cache: Any? = null

    val body = object : Body {
        override val raw: ByteArray
            get() {
                if (bodyTypeLock == BodyTypeLock.NONE) {
                    bodyTypeLock = BodyTypeLock.RAW
                    cache = _body.readBytes()
                }
                if (bodyTypeLock == BodyTypeLock.RAW) return cache as ByteArray
                throw IllegalStateException("Body already parsed as ${bodyTypeLock.name}")
            }
        override val json: ParsedElement
            get() {
                if (bodyTypeLock == BodyTypeLock.NONE) {
                    bodyTypeLock = BodyTypeLock.JSON
                    cache = KSON.parse(String(_body.readBytes()))
                }
                if (bodyTypeLock == BodyTypeLock.JSON) return cache as ParsedElement
                throw IllegalStateException("Body already parsed as ${bodyTypeLock.name}")
            }
        override val text: String
            get() {
                if (bodyTypeLock == BodyTypeLock.NONE) {
                    bodyTypeLock = BodyTypeLock.TEXT
                    cache = String(_body.readBytes())
                }
                if (bodyTypeLock == BodyTypeLock.TEXT) return cache as String
                throw IllegalStateException("Body already parsed as ${bodyTypeLock.name}")
            }
        override val stream: InputStream
            get() {
                if (bodyTypeLock == BodyTypeLock.NONE) {
                    bodyTypeLock = BodyTypeLock.STREAM
                    cache = _body
                }
                if (bodyTypeLock == BodyTypeLock.STREAM) return cache as InputStream
                throw IllegalStateException("Body already parsed as ${bodyTypeLock.name}")
            }
    }
}