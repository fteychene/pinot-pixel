import java.io.File
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val defaultFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS z")
val backupFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS z")
val backup2Format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S z")
val backup3Format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")

fun parseTime(from: String, format: DateTimeFormatter = defaultFormat): String =
    try {
        if (from.length == 27) from
        else defaultFormat.format(format.parse(from))
    } catch (e: DateTimeParseException) {
        when (format) {
            defaultFormat -> parseTime(from, backupFormat)
            backupFormat -> parseTime(from, backup2Format)
            backup2Format -> parseTime(from, backup3Format)
            else -> throw IllegalStateException("Timestamp $from is not a supported timestamp")
        }
    }


fun main() {
    for (i in 0..73) {
        println("Sanitize $i")
        val source = Paths.get("pinot", "rplace", "dataset_${String.format("%02d", i)}.csv")
        val target = Paths.get("pinot", "rplace", "dataset_sanitize_${String.format("%02d", i)}.csv")
        val targetFile = target.toFile()
        if (targetFile.exists()) {
            targetFile.delete()
        }
        targetFile.writer().use { writer ->
            writer.write("timestamp,user_id,pixel_color,x,y\n")
            source.toFile()
                .bufferedReader()
                .lineSequence()
                .drop(1)
                .map { it.split(",", limit = 4) }
                .filter { it[3].split(",").size == 2 }
                .forEach {
                    val coordinates = it[3].removePrefix("\"").removeSuffix("\"").split(",")
                    val newLine = listOf(
                        parseTime(it[0]),
                        it[1],
                        it[2],
                        coordinates[0],
                        coordinates[1]
                    )
                    writer.write("${newLine.joinToString(",")}\n")
                }
        }
    }
}