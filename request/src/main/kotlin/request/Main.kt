package request

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.pinot.client.ConnectionFactory
import org.apache.pinot.client.Request
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Gson
import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.sse
import org.http4k.routing.static
import org.http4k.server.PolyHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.sse.Sse
import org.http4k.sse.SseMessage
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

fun main() = runBlocking {
    val totalCounter = AtomicInteger(0)
    val counter = AtomicInteger(0)

    launch(Dispatchers.Default) {
        println("Start monitoring")
        while (true) {
            delay(1000L)
            println("Received ${totalCounter.get()} events and ${counter.get()} in sse")
        }
    }

    val zkUrl = "localhost:2181"
    val pinotClusterName = "PinotCluster"
    val pinotConnection = ConnectionFactory.fromZookeeper("$zkUrl/$pinotClusterName")

    val http = routes(
        "/index.html" bind Method.GET to static(),
        "/users" bind Method.GET to {
            val result = JsonArray()

            val pinotClientRequest =
                Request("sql", "SELECT user, COUNT(*) AS counter FROM pixelEvent GROUP BY user ORDER BY counter")
            val pinotResultSetGroup = pinotConnection.execute(pinotClientRequest)
            val resultTableResultSet = pinotResultSetGroup.getResultSet(0)

            for (i in 0 until resultTableResultSet.rowCount) {
                val jsonObject = JsonObject()
                for (colIndex in 0 until resultTableResultSet.columnCount) {
                    jsonObject
                        .addProperty(
                            resultTableResultSet.getColumnName(colIndex),
                            resultTableResultSet.getString(i, colIndex)
                        )
                }
                result.add(jsonObject)
            }

            Response(Status.OK).body(Gson.pretty(result))
        }
    )

    val sse = sse(
        "/stream" bind { sse: Sse ->
            var keepPolling = true
            Thread {
                KafkaConsumer<String, String>(kafkaProps("sse-${Thread.currentThread().id}")).use { kafkaClient ->
                    println("Start kafka polling")
                    kafkaClient.subscribe(listOf("pixels"))
                    try {
                        while (keepPolling) {
                            kafkaClient.poll(Duration.ofMillis(300)).toList()
                                .forEach {
                                    counter.incrementAndGet()
                                    sse.send(SseMessage.Data(it.value()))
                                }
                        }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
                println("Stopped kafka polling")
            }.start()
            sse.onClose {
                println("Closing sse")
                keepPolling = false
            }
        }
    )

    PolyHandler(http, sse = sse).asServer(Undertow(8081)).start()

    println()

}

fun kafkaProps(id: String = UUID.randomUUID().toString()): Map<String, String> =
    mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092",
        ConsumerConfig.GROUP_ID_CONFIG to "request",
        ConsumerConfig.CLIENT_ID_CONFIG to id,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
    )