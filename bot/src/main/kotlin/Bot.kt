import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.http4k.client.JavaHttpClient
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.format.Gson.auto
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.random.Random

data class ColorPixel(
    val x: Int,
    val y: Int,
    val color: HexColor,
    val user: String
)

val x = 100
val y = 100
val colorizeLens = Body.auto<ColorPixel>().toLens();

fun main() = runBlocking {
    val colors = listOf(
        HexColor("#ffffff"),
        HexColor("#000000"),
        HexColor("#ff0000"),
        HexColor("#00ff00"),
        HexColor("#0000ff")
    )

    for (id in 0..10) {
        launch(Dispatchers.Default) {
            val client: HttpHandler = JavaHttpClient()
            println("Started bot $id")
            val botId = "bot-$id"
            for (i in 1..1000000) {
                val request = Request(Method.PUT, "http://localhost:8080/colorize").with(
                    colorizeLens.of(
                        ColorPixel(
                            Random.nextInt(x),
                            Random.nextInt(y),
                            colors.random(),
                            botId
                        )
                    )
                )
                client(request)
            }
            println("Ending bot $id")
        }
    }

}