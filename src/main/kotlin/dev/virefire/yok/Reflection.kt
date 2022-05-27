package dev.virefire.yok

import dev.virefire.yok.types.Method
import java.lang.Exception
import java.lang.reflect.Field
import java.net.HttpURLConnection
import java.net.ProtocolException

internal fun setMethod(conn: HttpURLConnection, method: Method) {
    setRequestMethod(conn, method.name)
}

private fun setRequestMethod(conn: HttpURLConnection, method: String) {
    try {
        conn.requestMethod = method
    } catch (pe: ProtocolException) {
        try {
            conn.requestMethod = method
        } catch (pe: ProtocolException) {
            var connectionClass: Class<*>? = conn.javaClass
            val delegateField: Field?
            try {
                delegateField = connectionClass!!.getDeclaredField("delegate")
                delegateField.isAccessible = true
                val delegateConnection = delegateField[conn] as HttpURLConnection
                setRequestMethod(delegateConnection, method)
            } catch (ignored: NoSuchFieldException) {
            } catch (e: IllegalArgumentException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
            try {
                var methodField: Field
                while (connectionClass != null) {
                    try {
                        methodField = connectionClass.getDeclaredField("method")
                    } catch (e: NoSuchFieldException) {
                        connectionClass = connectionClass.superclass
                        continue
                    }
                    methodField.isAccessible = true
                    methodField[conn] = method
                    break
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}
