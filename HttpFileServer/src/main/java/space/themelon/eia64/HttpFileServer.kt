package space.themelon.eia64

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Paths

object HttpFileServer {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = HttpServer.create(InetSocketAddress(9876), 0)

        server.createContext("/file") { exchange: HttpExchange ->
            exchange.responseHeaders["Access-Control-Allow-Origin"] = "*"
            exchange.responseHeaders["Access-Control-Allow-Methods"] = "GET, POST, OPTIONS"
            exchange.responseHeaders["Access-Control-Allow-Headers"] = "Content-Type"

            val query = exchange.requestURI.query
            if (query != null && query.startsWith("name=")) {
                val filePath: String = query.split("=".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1]
                // redirect to stdlib
                val file = Paths.get("stdlib/$filePath").normalize()
                val response = Files.readAllBytes(file)

                exchange.sendResponseHeaders(200, response.size.toLong())
                val os = exchange.responseBody
                os.write(response)
                os.close()
            }
        }

        server.executor = null
        server.start()
    }
}
