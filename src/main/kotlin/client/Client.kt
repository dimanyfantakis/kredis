package client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.system.*

fun main() {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager)
            .tcp()
            .connect("127.0.0.1", 9002)
            .tls(coroutineContext = coroutineContext)

        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel(autoFlush = true)

        launch(Dispatchers.IO) {
            while (true) {
                val greeting = receiveChannel.readUTF8Line()
                if (greeting != null) {
                    println(greeting)
                } else {
                    println("Server closed a connection")
                    socket.close()
                    selectorManager.close()
                    exitProcess(0)
                }
            }
        }

        while (true) {
            val myMessage = readln()
            sendChannel.writeStringUtf8("$myMessage\n")
        }
    }
}