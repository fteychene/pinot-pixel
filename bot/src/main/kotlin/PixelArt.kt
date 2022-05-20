import kotlinx.coroutines.runBlocking
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

fun <T> Sequence<T>.repeat() = sequence { while (true) yieldAll(this@repeat) }

fun main() = runBlocking {
    println("Start pixel art bot")
    val source = listOf(
        "pixel.jpg",
        "ranni.jpg",
        "rick_astley.jpg"
    )
    source.asSequence().repeat()
        .forEach {
            val original = ImageIO.read(PixelArt::class.java.classLoader.getResourceAsStream(source.random())!!)
            val pixelArt = PixelArt().toPixelArt(original, x, y)

            val client: HttpHandler = JavaHttpClient()
            for (pixel in pixelArt.shuffled()) {
                val request = Request(Method.PUT, "http://localhost:8080/colorize").with(
                    colorizeLens.of(
                        ColorPixel(
                            pixel.x,
                            pixel.y,
                            HexColor(pixel.color),
                            "ranni-bot"
                        )
                    )
                )
                client(request)
            }
            Thread.sleep(10000)
        }

}

data class Pixel(
    val x: Int,
    val y: Int,
    val color: String
)

class PixelArt {

    fun toPixelArt(original: BufferedImage, horiPixelCount: Int, vertiPixelCount: Int): List<Pixel> {
        // return a bufferedimage that is the same size as the original image but
        // pixelized
        val pixelWidth = Math.round(original.width / horiPixelCount.toDouble()).toInt()
        val pixelHeight = Math.round(original.height / vertiPixelCount.toDouble()).toInt()
        val pixelArt = mutableListOf<Pixel>()
        val originalPixels = convertTo2DIntArr(original)
        for (i in 0 until vertiPixelCount) {
            for (ii in 0 until horiPixelCount) {
                var A: Long = 0
                var R: Long = 0
                var G: Long = 0
                var B: Long = 0
                for (y in 0 until pixelHeight) {
                    for (x in 0 until pixelWidth) {
                        if (i * pixelHeight + y < original.height && ii * pixelWidth + x < original.width) {
                            A += originalPixels[0][i * pixelHeight + y][ii * pixelWidth + x].toLong()
                            R += originalPixels[1][i * pixelHeight + y][ii * pixelWidth + x].toLong()
                            G += originalPixels[2][i * pixelHeight + y][ii * pixelWidth + x].toLong()
                            B += originalPixels[3][i * pixelHeight + y][ii * pixelWidth + x].toLong()
                        }
                    }
                }
                A /= (pixelHeight * pixelWidth).toLong()
                R /= (pixelHeight * pixelWidth).toLong()
                G /= (pixelHeight * pixelWidth).toLong()
                B /= (pixelHeight * pixelWidth).toLong()

                pixelArt.add(
                    Pixel(
                        ii,
                        i,
                        "#${String.format("%02X", R)}${String.format("%02X", G)}${String.format("%02X", B)}"
                    )
                )

            }
        }
        return pixelArt
    }

    fun convertTo2DIntArr(image: BufferedImage): Array<Array<IntArray>> {
        val width = image.width
        val height = image.height
        val result = Array(4) {
            Array(height) {
                IntArray(
                    width
                )
            }
        }
        var pixel = 0
        var row = 0
        var col = 0
        while (pixel < width * height) {
            val curPixel = image.getRGB(col, row)
            result[0][row][col] = curPixel and -0x1000000 shr 24 // a
            result[1][row][col] = curPixel and 0x00FF0000 shr 16 // r
            result[2][row][col] = curPixel and 0x0000FF00 shr 8 // g
            result[3][row][col] = curPixel and 0x000000FF // b
            col++
            if (col == width) {
                col = 0
                row++
            }
            pixel++
        }
        return result
    }
}