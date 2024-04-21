package server.cache

interface Cache {

    operator fun set(key: RespType, value: RespType)

    operator fun get(key: RespType): RespType?

}

object LRUCache : Cache {
    // TODO: Make concurrent.
    private val cache: HashMap<RespType, Node> = hashMapOf()
    private val least: Node = Node(null, null)
    private val most: Node  = Node(null, null)

    var size: Int = cache.size

    init {
        least.prev = most
        most.next  = least
    }


    @Override
    override fun set(key: RespType, value: RespType) {
        // Remove from cache.
        cache[key]?.let {
            remove(it)
        }
        val node = Node(key, value)
        // Update the map value.
        cache[key] = node
        // Insert to cache.
        add(node)
        // Update size.
        size = cache.size
    }

    @Override
    override fun get(key: RespType): RespType? {
        return cache[key]?.let {
            // Remove from cache.
            remove(it)
            // Insert to cache.
            add(it)
            it.value
        }
    }

    internal data class Node(val key:   RespType?,
                             val value: RespType?) {
        var next: Node? = null
        var prev: Node? = null
    }


    // Small utility methods.

    private fun add(node: Node) {
        val mostCopy: Node? = most.next
        // Update the head.
        most.next = node
        node.prev = most

        mostCopy?.prev = node
        node.next = mostCopy
    }

    private fun remove(node: Node) {
        val prevCopy: Node? = node.prev
        val nextCopy: Node? = node.next

        prevCopy?.next = nextCopy
        nextCopy?.prev = prevCopy
    }

    private fun clear() {
        least.next = most
        most.prev  = least
        cache.clear()
    }

    fun getHead(): Pair<RespType?, RespType?> {
        return Pair(most.next?.key, most.next?.value)
    }

    fun getTail(): Pair<RespType?, RespType?> {
        return Pair(least.prev?.key, least.prev?.value)
    }
}
