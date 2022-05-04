package dev.virefire.yok.types

import dev.virefire.yok.Yok

class InterceptorContext internal constructor(val req: Request, val client: Yok, val next: () -> Response)

typealias InterceptorHandler = InterceptorContext.() -> Response

class Interceptor internal constructor(internal val handler: InterceptorHandler) {
    internal fun intercept(req: Request, client: Yok, next: () -> Response): Response {
        return handler(InterceptorContext(req, client, next))
    }
}

fun interceptor (handler: InterceptorHandler): Interceptor {
    return Interceptor(handler)
}