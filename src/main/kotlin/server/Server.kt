package server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import server.cache.Cache
import server.cache.LRUCache
import server.cache.RespType
import server.cache.Type
import java.math.BigInteger


val cache: Cache = LRUCache


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
                sendChannel.writeStringUtf8("Please enter your name\n")
                try {
                    while (true) {
                        val request = receiveChannel.readUTF8Line()?.split(" ")
                        request?.let {
                            executeCommand(request.rest(1)).let { response ->
                                sendChannel.writeStringUtf8(response)
                            }
                        } ?: run {
                            // Input is null.
                            // Throw Exception?
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

typealias Command = (arguments: List<String>) -> String

fun get(): Command = { arguments ->
    arguments.firstOrNull()?.let {key ->
        // Parse the key.
        val x = key.substring(1)
        val respType = when (key.firstOrNull()) {
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
        // Construct the response.
        constructResponse(respType) + CRLF
    } ?:
        // Throw exception?
        ""
}

fun set(): Command = { arguments ->
    val key = arguments.firstOrNull()
    val rest = arguments.rest(1)
    key?.let {
        rest.firstOrNull()?.let {
            // Parse the key.
            // Parse the value.
            // Set the <key,value> pair.
//            cache[key] = value
        } ?: run {
            // No value.
            // Throw exception.
        }
        ""
        // Construct response.
    } ?:
        // No key.
        // Throw exception.
        ""
}

fun mset(): Command = {
    TODO()
}


fun mget(): Command = {
    TODO()
}

fun flush(): Command = {
    TODO()
}

fun delete(): Command = {
    TODO()
}

// Utility functions.

/**
 * This function constructs the Server's response based on the RespType by
 * retrieving the value from the Cache.
 *
 * @param x The RespType.
 */
fun constructResponse(x: RespType): String {
    return cache[x]?.let {
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

/**
 * This function is responsible for constructing the RespType.
 *
 * @param x1 The Type of the request.
 * @param x  The value that the RespType holds.
 */
fun parseRespType(x1: Type, x: String): RespType {
    val x2 = x.removeSuffix(CRLF)
    return when (x1) {
        Type.STRING,
        Type.INT,
        Type.BOOLEAN,
        Type.BIGINTEGER,
        Type.DOUBLE -> getBasicType(x1, x2)
         // $<length>\r\n<data>\r\n , length = bytes
        Type.BYTEARRAY -> {
            val split = x2.split(CRLF)
            if (split[0].toInt() == split[1].length) RespType.ByteArray(split[1].toByteArray()) else RespType.ByteArray("".toByteArray())
        }
        Type.NULL -> RespType.Null(x2)
        Type.ERROR -> RespType.Error(x2)
        // Can contain mix types.
        Type.ARRAY -> {
            val split = x2.split(CRLF)
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
 * @param x1 The Type of the request.
 * @param x  The value that the RespType holds.
 */
fun getBasicType(x1: Type, x: String): RespType {
    return when (x1) {
        Type.STRING     -> RespType.String(x)
        Type.INT        -> x.toIntOrNull()?.let { RespType.Int(it) } ?: RespType.Int(0)
        Type.BOOLEAN    -> RespType.Boolean(x.toBoolean())
        Type.BIGINTEGER -> x.toBigIntegerOrNull()?.let { RespType.BigInteger(it) } ?: RespType.BigInteger(BigInteger.valueOf(0))
        // ,[<+|->]<integral>[.<fractional>][<E|e>[sign]<exponent>]\r\n
        // ,[inf | -inf | nan]
        Type.DOUBLE     -> TODO()
        // Not reachable.
        else -> RespType.Int(0)
    }
}

fun List<String>.rest(fromIndex: Int) = this.subList(fromIndex, this.size)
