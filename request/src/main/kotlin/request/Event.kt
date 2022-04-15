package request

import java.time.ZonedDateTime


sealed class Event
data class PixelUpdated(val user: String, val time: Long, val pixel: Pixel)
