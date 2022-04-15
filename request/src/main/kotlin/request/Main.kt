package request

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.http4k.format.Gson
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

fun main() = runBlocking {
    val counter = AtomicInteger(0);
    val kafkaClient = KafkaConsumer<String, String>(kafkaProps())

    launch(Dispatchers.Default) {
        println("Start monitoring")
        while (true) {
            delay(1000L)
            println("Received ${counter.get()} events")
        }
    }

    kafkaClient.use {
        it.subscribe(listOf("pixels"))
        println("Start polling")
        while (true) {
            it.poll(Duration.ofSeconds(3)).toList().forEach { record ->
                val e = Gson.mapper.fromJson(record.value(), PixelUpdated::class.java)
                counter.incrementAndGet()
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