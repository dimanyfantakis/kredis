package server.cache

interface Cache {

    operator fun set(key: RespType, value: RespType)

    operator fun get(key: RespType): RespType?

}

object LRUCache : Cache {
    private val cache: HashMap<RespType, Node> = hashMapOf()
    private val least: Node = Node(null, null)
    private val most: Node  = Node(null, null)

    init {
        least.next = most
        most.prev  = least
    }


    @Override
    override fun set(key: RespType, value: RespType) {
        cache[key]?.let {
            // Remove from cache.
            remove(it)
        }
        val node = Node(key, value)
        // Update the map value.
        cache[key] = node
        // Insert to cache.
        add(node)
    }

    @Override
    override fun get(key: RespType): RespType? {
        return cache[key]?.let {
            // Remove from cache.
            remove(it)
            // Insert to cache.
            add(it)
            // Return val.
            it.value
        } ?: run {
            null
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
        // Update most.
        most.next = node
        node.prev = most
        // New points to prev most.
        mostCopy?.prev = node
        node.next = mostCopy
    }

    private fun remove(node: Node) {
        // remove node.
        val prevCopy: Node? = node.prev
        val nextCopy: Node? = node.next

        prevCopy?.next = nextCopy
        nextCopy?.prev = prevCopy
    }

    private fun clear() {
        // Reset most and prev.
        least.next = most
        most.prev  = least
        cache.clear()
    }
}
