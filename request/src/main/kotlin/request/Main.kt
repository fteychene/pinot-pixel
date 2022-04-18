package request

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.pinot.client.ConnectionFactory
import org.apache.pinot.client.Request
import org.http4k.format.Gson
import org.http4k.format.Gson.asJsonObject
import java.io.File
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

val SEGMENT_SIZE = 500000
val export = false

fun main() = runBlocking {
    val totalCounter = AtomicInteger(0)
    val counter = AtomicInteger(0)
    var segmentNumber = 0
    val kafkaClient = KafkaConsumer<String, String>(kafkaProps())

    launch(Dispatchers.Default) {
        println("Start monitoring")
        while (true) {
            delay(1000L)
            println("Received ${totalCounter.get()} events")
        }
    }

    launch(Dispatchers.Default) {
        // pinot connection
        val zkUrl = "localhost:2181"
        val pinotClusterName = "PinotCluster"
        val pinotConnection = ConnectionFactory.fromZookeeper("$zkUrl/$pinotClusterName")
        println("Start querying")
        while (true) {
            delay(5000L)
            // set queryType=sql for querying the sql endpoint
            val pinotClientRequest = Request("sql", "SELECT user, COUNT(*) AS counter FROM pixelEvent GROUP BY user ORDER BY counter")
            val pinotResultSetGroup = pinotConnection.execute(pinotClientRequest)
            val resultTableResultSet = pinotResultSetGroup.getResultSet(0)

            val numRows = resultTableResultSet.rowCount
            val numColumns = resultTableResultSet.columnCount
            println("Result ($numRows rows, $numColumns columns)")
            for (i in 0 until numRows) {
                println("(${resultTableResultSet.getColumnName(0)}) ${resultTableResultSet.getString(i, 0)} (${resultTableResultSet.getColumnName(1)}) ${resultTableResultSet.getString(i, 1)}")
            }
        }
    }

    kafkaClient.use {
        it.subscribe(listOf("pixels"))
        println("Start polling")
        val events = mutableListOf<PixelUpdated>()
        while (true) {
            it.poll(Duration.ofSeconds(3)).toList().forEach { record ->
                val e = Gson.mapper.fromJson(record.value(), PixelUpdated::class.java)
                events.add(e)
                totalCounter.incrementAndGet()
                if (export && counter.incrementAndGet() == SEGMENT_SIZE) {
                    File(
                        "pixel_updated_${
                            String.format(
                                "%02d",
                                segmentNumber
                            )
                        }.json"
                    ).writeBytes(Gson.pretty(events.asJsonObject()).toByteArray())
                    println("Written segment")
                    events.clear()
                    counter.set(0)
                    segmentNumber += 1
                }
            }
            it.commitAsync()
        }
    }
}

fun kafkaProps(): Map<String, String> =
    mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092",
        ConsumerConfig.GROUP_ID_CONFIG to UUID.randomUUID().toString(),
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
    )