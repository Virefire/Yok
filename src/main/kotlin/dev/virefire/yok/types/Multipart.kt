package dev.virefire.yok.types

import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import java.io.File
import java.io.InputStream

class MultipartBuilder internal constructor() {
    private val builder: MultipartEntityBuilder = MultipartEntityBuilder.create()
    data class PartMetadata(
        val name: String,
        var contentType: ContentType = ContentType.DEFAULT_BINARY,
        var filename: String? = null
    )

    infix fun String.type(contentType: String): PartMetadata {
        return PartMetadata(this, contentType = ContentType.create(contentType))
    }

    infix fun String.filename(filename: String): PartMetadata {
        return PartMetadata(this, filename = filename)
    }

    infix fun PartMetadata.type(contentType: String): PartMetadata {
        this.contentType = ContentType.create(contentType)
        return this
    }

    infix fun PartMetadata.filename(filename: String): PartMetadata {
        this.filename = filename
        return this
    }

    infix fun String.to(part: String) {
        builder.addTextBody(this, part)
    }

    infix fun String.to(part: File) {
        builder.addBinaryBody(this, part)
    }

    infix fun String.to(part: ByteArray) {
        builder.addBinaryBody(this, part)
    }

    infix fun String.to(part: InputStream) {
        builder.addBinaryBody(this, part)
    }

    infix fun PartMetadata.to(part: String) {
        builder.addTextBody(this.name, part, this.contentType)
    }

    infix fun PartMetadata.to(part: File) {
        builder.addBinaryBody(this.name, part, this.contentType, this.filename)
    }

    infix fun PartMetadata.to(part: ByteArray) {
        builder.addBinaryBody(this.name, part, this.contentType, this.filename)
    }

    infix fun PartMetadata.to(part: InputStream) {
        builder.addBinaryBody(this.name, part, this.contentType, this.filename)
    }

    internal fun build(): HttpEntity {
        return builder.build()
    }
}

fun multipart(builder: MultipartBuilder.() -> Unit): MultipartBuilder {
    val multipartBuilder = MultipartBuilder()
    builder(multipartBuilder)
    return multipartBuilder
}