package dev.virefire.yok.types

class Headers : MutableMap<String, String> {
    val list = mutableListOf<Pair<String, String>>()

    override val entries: MutableSet<MutableMap.MutableEntry<String, String>>
        get() = list.map {
            object : MutableMap.MutableEntry<String, String> {
                override val key: String
                    get() = it.first
                override val value: String
                    get() = it.second
                override fun setValue(newValue: String): String {
                    val oldValue = value
                    list.remove(it)
                    list.add(Pair(key, newValue))
                    return oldValue
                }
            }
        }.toMutableSet()

    override val keys: MutableSet<String>
        get() = list.map { it.first }.toMutableSet()

    override val size: Int
        get() = list.size

    override val values: MutableCollection<String>
        get() = list.map { it.second }.toMutableSet()

    override fun containsKey(key: String): Boolean {
        return list.any { it.first.equals(key, true) }
    }

    override fun containsValue(value: String): Boolean {
        return list.any { it.second == value }
    }

    override fun get(key: String): String? {
        return list.lastOrNull { it.first.equals(key, true) }?.second
    }

    fun getAll(): MutableList<Pair<String, String>> {
        return list
    }

    fun getAll(key: String): List<String> {
        return list.filter { it.first.equals(key, true) }.map { it.second }
    }

    fun addAll(list: List<Pair<String, String>>) {
        this.list.addAll(list)
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun clear() {
        list.clear()
    }

    override fun putAll(from: Map<out String, String>) {
        from.forEach {
            list.add(Pair(it.key, it.value))
        }
    }

    override fun remove(key: String): String? {
        var lastValue: String? = null
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.first.equals(key, true)) {
                lastValue = next.second
                iterator.remove()
            }
        }
        return lastValue
    }

    override fun put(key: String, value: String): String? {
        val lastValue = remove(key)
        list.add(Pair(key, value))
        return lastValue
    }

    fun add(key: String, value: String) {
        list.add(Pair(key, value))
    }

    operator fun plus(pair: Pair<String, String>): Headers {
        val newHeaders = Headers()
        newHeaders.list.addAll(this@Headers.list)
        newHeaders.list.add(pair)
        return newHeaders
    }

    operator fun plus(headers: Headers): Headers {
        val newHeaders = Headers()
        newHeaders.list.addAll(this@Headers.list)
        newHeaders.list.addAll(headers.list)
        return newHeaders
    }
}

fun headersOf(vararg pairs: Pair<String, String>): Headers {
    val headers = Headers()
    pairs.forEach {
        headers.add(it.first, it.second)
    }
    return headers
}