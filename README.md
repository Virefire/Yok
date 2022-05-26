<div align="center"><h1>Yok - Kotlin HTTP request library</h1></div>

<div align="center"><img alt="Logo" src="./logo.png"/></div>

<div align="center">
    <img alt="Open issues" src="https://img.shields.io/github/issues-raw/Virefire/Yok"/>
    <img alt="Open issues" src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.rikonardo.com%2Freleases%2Fdev%2Fvirefire%2Fyok%2FYok%2Fmaven-metadata.xml"/>
    <img alt="Open issues" src="https://img.shields.io/github/languages/code-size/Virefire/Yok"/>
</div>

<br>

<hr>

💼 **This readme contains full library documentation/tutorial!**

## Install

Gradle Kotlin:
```kotlin
repositories {
    maven {
        url = uri("https://maven.rikonardo.com/releases")
    }
}

dependencies {
    implementation("dev.virefire.yok:Yok:1.0.3")
}
```

## Documentation

| Content                                  |
|------------------------------------------|
| **1. [Simple request](#simple-request)** |
| **2. [Client](#client)**                 |
| **3. [Sending data](#sending-data)**     |
| **4. [Response](#response)**             |
| **5. [Receiving data](#receiving-data)** |
| **6. [Interceptors](#interceptors)**     |

### Simple request
Here is a simple example of how to use Yok library:

```kotlin
fun main() {
    val response = Yok.get("https://httpbin.org/get")
    println(response.body.text)
}
```

Optionally you can specify additional request settings:

```kotlin
fun main() {
    val response = Yok.get("https://httpbin.org/get") {
        headers = headersOf("User-Agent" to "HelloWorld/1.0")
    }
    println(response.body.text)
}
```

Notice that headers has custom type and initialized with headersOf(). This is because you can specify multiple headers with the same name.

You can also send a request like this, for example to pass a method from somewhere else:

```kotlin
fun main() {
    val response = Yok.request {
        url = "https://httpbin.org/get"
        method = Method.GET
    }
    println(response.body.text)
}
```

Here is a list of all available request settings:

| Setting                 | Description                           | Default                 |
|-------------------------|---------------------------------------|-------------------------|
| **url**                 | Request URL                           |                         |
| **method**              | HTTP method                           |                         |
| **headers**             | Headers                               | `headersOf()`           |
| **params**              | URL params                            | `emptyMap()`            |
| **query**               | Query params                          | `emptyMap()`            |
| **proxy**               | Proxy                                 | _Inherited from client_ |
| **connectTimeout**      | Connection timeout                    | _Inherited from client_ |
| **readTimeout**         | Read timeout                          | _Inherited from client_ |
| **followRedirects**     | Should automatically follow redirects | _Inherited from client_ |
| **addDefaultUserAgent** | Should automatically add UserAgent    | _Inherited from client_ |

### Client
Optionally, you can create a client to make requests with some default settings:

```kotlin
val client = Yok {
    baseUrl = "https://httpbin.org"
    headers = headersOf("User-Agent" to "HelloWorld/1.0")
}

fun main() {
    val response = client.get("/get")
    println(response.body.text)
}
```

List of all available client settings:

| Setting                 | Description                           | Default       |
|-------------------------|---------------------------------------|---------------|
| **baseUrl**             | Base URL                              |               |
| **headers**             | Headers                               | `headersOf()` |
| **proxy**               | Proxy                                 |               |
| **connectTimeout**      | Connection timeout                    | `10000` ms    |
| **readTimeout**         | Read timeout                          | `10000` ms    |
| **followRedirects**     | Should automatically follow redirects | `true`        |
| **addDefaultUserAgent** | Should automatically add UserAgent    | `true`        |

### Sending data

Yok comes with some built-in data serializers. You can use them to send data in the request body:

```kotlin
fun main() {
    Yok.post("https://httpbin.org/post") {
        body.text = "Hello World"
    }
    Yok.post("https://httpbin.org/post") {
        body.json = mapOf(
            "name" to "Satoshi",
            "age" to "42"
        )
    }
    Yok.post("https://httpbin.org/post") {
        body.json = object { // JSON with anonymous object, serialized with KSON (https://github.com/Virefire/KSON)
            val name = "Satoshi"
            val age = "42"
        }
    }
    Yok.post("https://httpbin.org/post") {
        body.form = mapOf(
            "name" to "Satoshi",
            "age" to "42"
        )
    }
    Yok.post("https://httpbin.org/post") {
        body.stream = this.javaClass.getResourceAsStream("/test.txt")
    }
    Yok.post("https://httpbin.org/post") {
        body.multipart = multipart {
            "file" to File("/test.txt")
            "file2" type "text/plain" to File("/test2.txt").readBytes()
            "file3" filename "test3.txt" to File("/test3.txt").readBytes()
            "text" to "Hello World"
        }
    }
    Yok.post("https://httpbin.org/post") {
        body.raw = ByteArray()
    }
}
```

### Response

Response object contains all the information about the response:

```kotlin
fun main() {
    val response = Yok.get("https://httpbin.org/get")
    println(response.status)
    response.headers.forEach { println("${it.key}: ${it.value}") }
}
```

### Receiving data

Yok also has parsers for response body. You can use them to parse response body:

```kotlin
fun main() {
    val res1 = Yok.get("https://httpbin.org/get")
    println(res1.body.raw) // Raw ByteArray
    val res2 = Yok.get("https://httpbin.org/get")
    println(res2.body.text) // String
    val res3 = Yok.get("https://httpbin.org/get")
    println(res3.body.json) // KSON`s ParsedElement (https://github.com/Virefire/KSON)
    val res4 = Yok.get("https://httpbin.org/get")
    println(res4.body.stream) // Raw InputStream
}
```

Note, that once you accessed specific parser, you can't access other parsers, this will cause exception.

### Interceptors

There is also simple interceptor system, which allows you to intercept requests and responses.

Here is a practical example with authorization:

```kotlin
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
    val client = Yok { baseUrl = "https://api.example.com" }
    val res = client.get("/user") { interceptor(auth) }
    println(res.body.json["name"].string)
}
```

Note that in context of interceptor, there are 3 fields available: `req`, which is request object, `next` which is trigger for next interceptor (cannot be called twice), and `client` which is client object.