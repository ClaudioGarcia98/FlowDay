package dev.flowday.core.database.util

import androidx.room.TypeConverter
import java.time.Instant

class Converters {

    @TypeConverter
    fun convertLongToInstant(value: Long?): Instant? =
        value?.let(Instant::ofEpochSecond)


    @TypeConverter
    fun convertInstantToLong(value: Instant?): Long? =
        value?.epochSecond
}