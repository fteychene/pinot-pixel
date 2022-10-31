package command

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun main() {
    val gson = Gson()
    val originalBasedTime = Instant.ofEpochMilli(1650122870142)
        .atZone(ZoneId.of("UTC"))
    val targetBase = Instant.now()
        .atZone(ZoneId.of("UTC"))
        .minus(30, ChronoUnit.DAYS)
    val timeToAdd = targetBase.toInstant().toEpochMilli() - originalBasedTime.toInstant().toEpochMilli();
    for (i in 0..43) {
        println("Update $i")
        val source = Paths.get("pinot", "hybrid_data", "pixel_updated_${String.format("%02d", i)}.json")
        val target = Paths.get("pinot", "hybrid_data", "pixel_updated_closer_${String.format("%02d", i)}.json")
        val targetFile = target.toFile()
        if (targetFile.exists()) {
            targetFile.delete()
        }
        val reader = JsonReader(source.toFile().bufferedReader())
        val typeToken = object : TypeToken<List<PixelUpdated>>() {}.type
        val myData: List<PixelUpdated> = gson.fromJson(reader, typeToken)
        val updatedData = myData
            .map { it.copy(time = it.time + timeToAdd) }


        targetFile.writer().use { writer ->
            gson.toJson(updatedData, writer)

        }
    }
}