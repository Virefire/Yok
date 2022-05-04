package dev.virefire.yok

import dev.virefire.yok.types.interceptor

val refreshToken = "refresh_token"
var accessToken = "access_token"

val auth = interceptor {
    req.headers["Authorization"] = "Bearer $accessToken"
    var response = next()
    if (response.status == 401) {
        val refreshRes = client.post("/token") {
            body.json = mapOf(
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken
            )
        }
        accessToken = refreshRes.body.json["access_token"].string!!
        req.headers["Authorization"] = "Bearer $accessToken"
        response = client.request {
            inherit(req)
        }
    }
    return@interceptor response
}

fun main() {
    val response = Yok.get("https://httpbin.org/get")
    println(response.status)
    response.headers.forEach { println("${it.key}: ${it.value}") }
}