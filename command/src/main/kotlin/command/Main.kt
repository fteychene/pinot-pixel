package command

import arrow.core.Either
import arrow.core.Validated
import arrow.core.ValidatedNel
import arrow.core.flatMap
import arrow.core.identity
import arrow.core.zip
import arrow.typeclasses.Semigroup
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Gson
import org.http4k.format.Gson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.time.ZonedDateTime
import java.util.UUID

data class ColorPixel(
    val x: Int,
    val y: Int,
    val color: HexColor,
    val user: String
)

val SIZE = 50

fun main() {

    val colorLens = Body.auto<List<Color>>().toLens();
    val colorizeLens = Body.auto<ColorPixel>().toLens();

    val colors = listOf(
        Color("black", HexColor("#000000")),
        Color("white", HexColor("#ffffff"))
    )

    val kafkaClient = KafkaProducer<String, String>(kafkaProps())

    val router = routes(
        "/colors" bind Method.GET to {
            Response(Status.OK).with(colorLens.of(colors))
        },
        "/colorize" bind Method.PUT to { request ->
            Either.catch({ Response(Status.BAD_REQUEST).body("Invalid body received : ${it.message}") }) { colorizeLens(request) }
                .flatMap { colorize ->
                    colorize.x.inRange(0, SIZE)
                        .zip(Semigroup.nonEmptyList(), colorize.y.inRange(0, SIZE)) { x, y ->
                            PixelUpdated(colorize.user, ZonedDateTime.now().toInstant().toEpochMilli(), Pixel(x, y, colorize.color))
                        }
                        .mapLeft { Response(Status.BAD_REQUEST).body(it.joinToString("\n")) }
                        .toEither()
                }.flatMap { event ->
                    Either.catch {
                        kafkaClient.send(ProducerRecord("pixels", UUID.randomUUID().toString(), Gson.asFormatString(event))) { metadata, exception ->
                            if (exception != null) {
                                throw exception
                            }
//                            else {
//                                println("Inserted into kafka $metadata")
//                            }
                        }
                    }.mapLeft { Response(Status.INTERNAL_SERVER_ERROR).body( "Error sending into kafka ${it.message}") }
                }
                .fold(::identity) { Response(Status.OK) }

        }
    )
    router.asServer(Undertow(8080))
        .start()
}

fun Int.positive(): ValidatedNel<String, Int> =
    if (this < 0) Validated.invalidNel("$this not a positive int")
    else Validated.validNel(this)

fun Int.inRange(min: Int, max: Int): ValidatedNel<String, Int> =
    if (this in min until max) Validated.validNel(this)
    else Validated.invalidNel("$this not in range $min..$max")


fun kafkaProps(): Map<String, String> =
    mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.ACKS_CONFIG to "all",
        ProducerConfig.LINGER_MS_CONFIG to "20"
    )

fun <K, V> KafkaProducer<K, V>.sendAsync(record: ProducerRecord<K, V>): Deferred<RecordMetadata> =
    CompletableDeferred<RecordMetadata>().apply {
        send(record) { metadata, exception ->
            if (exception != null) {
                completeExceptionally(exception)
            } else {
                complete(metadata)
            }
        }
    }
