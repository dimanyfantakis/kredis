package server

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import java.math.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import server.cache.Cache
import server.cache.LRUCache
import server.cache.RespType
import server.cache.Type


val cache: LRUCache = LRUCache


fun main() {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager)
            .tcp()
            .bind("127.0.0.1", 9002)

        println("Server is listening at ${serverSocket.localAddress}")
        while (true) {
            val socket = serverSocket.accept()
            println("Accepted $socket")
            launch {
                val receiveChannel = socket.openReadChannel()
                val sendChannel = socket.openWriteChannel(autoFlush = true)
                sendChannel.writeStringUtf8("Enter your request\n")
                try {
                    while (true) {
                        val request = receiveChannel.readUTF8Line()?.split(" ")
                        request?.let {
                            executeCommand(it).let { response ->
                                sendChannel.writeStringUtf8(response)
                            }
                        } ?: run {
                            throw IllegalArgumentException("Request is null")
                        }
                    }
                } catch (e: Throwable) {
                    socket.close()
                }
            }
        }
    }
}

fun executeCommand(request: List<String>): String {
    val arguments = request.rest(1)

    return when (request[0]) {
        "GET"    -> get()    // <key>
        "SET"    -> set()    // <key> <value>
        "DELETE" -> delete() // <key>
        "FLUSH"  -> flush()
        "MGET"   -> mget()   // <key1> ... <keyn>
        "MSET"   -> mset()   // <key1> <value1> ... <keyn> <valuen>
        else -> null
    }?.let { command ->
        command(arguments)
    } ?: run {
        error("Not Valid Command")
    }
}

private const val CRLF = "\\r\\n"
private val OK = Type.STRING.symbol + "OK" + CRLF

typealias Command = (arguments: List<String>) -> String

fun get(): Command = { arguments ->
    arguments.firstOrNull()?.let { key ->
        // Parse the key.
        val respType = parseType(key)
        // Construct the response.
        constructResponse(respType) + CRLF
    } ?: throw IllegalArgumentException("No key passed")
}

fun set(): Command = { arguments ->
    arguments.firstOrNull()?.let {
        val key = parseType(it)

        arguments.getOrNull(1)?.let {
            val value = parseType(it)
            cache[key] = value
        } ?: throw IllegalArgumentException("No value passed")

    } ?: throw IllegalArgumentException("No key passed")

    // Everything went well, return an OK value.
    OK
}

fun mset(): Command = {
    TODO("Not yet implemented")
}


fun mget(): Command = {
    TODO("Not yet implemented")
}

fun flush(): Command = {
    TODO("Not yet implemented")
}

fun delete(): Command = {
    TODO("Not yet implemented")
}

// Utility functions.

/**
 * This function constructs the Server's response based on the RespType by
 * retrieving the value from the Cache.
 *
 * @param respType The RespType.
 */
fun constructResponse(respType: RespType): String {
    return cache[respType]?.let {
        when(it) {
            is RespType.String -> Type.STRING.symbol + it.string
            is RespType.Int -> Type.INT.symbol + "${it.int}"
            is RespType.Boolean -> Type.BOOLEAN.symbol + "${it.boolean}"
            is RespType.BigInteger -> Type.BIGINTEGER.symbol + "${it.bigInteger}"
            is RespType.Double -> Type.DOUBLE.symbol + "${it.double}"
            is RespType.ByteArray -> Type.BYTEARRAY.symbol + "${it.byteArray}"
            is RespType.Error -> Type.ERROR.symbol + "$it"
            is RespType.Null -> "${Type.NULL.symbol}"
            is RespType.Array -> TODO()
            is RespType.Map -> TODO()
            is RespType.Set -> TODO()
        }
    } ?: run {
        "${Type.NULL.symbol}"
    }
}

fun parseType(key: String) : RespType {
    val x = key.substring(1)

    return when (key.firstOrNull()) {
        '+' -> parseRespType(Type.STRING, x)
        ':' -> parseRespType(Type.INT, x)
        '#' -> parseRespType(Type.BOOLEAN, x)
        '(' -> parseRespType(Type.BIGINTEGER, x)
        ',' -> parseRespType(Type.DOUBLE, x)
        '$' -> parseRespType(Type.BYTEARRAY, x)
        '*' -> parseRespType(Type.ARRAY, x)
        '%' -> parseRespType(Type.MAP, x)
        '~' -> parseRespType(Type.SET, x)
        '_' -> parseRespType(Type.NULL, x)
        else -> parseRespType(Type.ERROR, x)
    }
}

/**
 * This function is responsible for constructing the RespType.
 *
 * @param type The Type of the request.
 * @param value  The value that the RespType holds.
 */
fun parseRespType(type: Type, value: String): RespType {
    val parsedValue = value.removeSuffix(CRLF)
    return when (type) {
        Type.STRING,
        Type.INT,
        Type.BOOLEAN,
        Type.BIGINTEGER,
        Type.DOUBLE    -> getBasicType(type, parsedValue)
        Type.BYTEARRAY -> {
            // $<length>\r\n<data>\r\n , length = bytes
            val split = parsedValue.split(CRLF)
            if (split[0].toInt() == split[1].length) RespType.ByteArray(split[1].toByteArray()) else RespType.ByteArray("".toByteArray())
        }
        Type.NULL  -> RespType.Null(parsedValue)
        Type.ERROR -> RespType.Error(parsedValue)
        // Can contain mix types.
        Type.ARRAY -> {
            val split = parsedValue.split(CRLF)
//            if (split[0].toInt() == split[1].length) RespType.Array(Array(split[0].toInt()) { i -> getBasicType(parseType()) }) else RespType.Array(arrayOf())
            TODO()
        }
        // *<number-of-elements>\r\n<element-1>...<element-n>
        Type.MAP -> TODO() // %<number-of-entries>\r\n<key-1><value-1>...<key-n><value-n>
        // key value pairs must be constructed in the same way
        Type.SET -> TODO() // <number-of-elements>\r\n<element-1>...<element-n>
    }
}

/**
 * This function is responsible for constructing the basic RespType.
 * A basic RespType is one that doesn't contain other RespTypes.
 *
 * @param type The Type of the request.
 * @param value  The value that the RespType holds.
 */
fun getBasicType(type: Type, value: String): RespType {
    return when (type) {
        Type.STRING     -> RespType.String(value)
        Type.INT        -> value.toIntOrNull()?.let { RespType.Int(it) } ?: RespType.Int(0)
        Type.BOOLEAN    -> RespType.Boolean(value.toBoolean())
        Type.BIGINTEGER -> value.toBigIntegerOrNull()?.let { RespType.BigInteger(it) } ?: RespType.BigInteger(BigInteger.valueOf(0))
        // ,[<+|->]<integral>[.<fractional>][<E|e>[sign]<exponent>]\r\n
        // ,[inf | -inf | nan]
        Type.DOUBLE     -> TODO()
        // Not reachable.
        else -> RespType.Int(0)
    }
}

fun List<String>.rest(fromIndex: Int) = this.subList(fromIndex, this.size)
