package io.burba.tothecomments.database.util

import android.arch.persistence.room.TypeConverter
import io.burba.tothecomments.database.models.CommentPage
import org.threeten.bp.Instant

class Converters {
    companion object {
        @TypeConverter @JvmStatic
        fun fromEpochMs(value: Long) = Instant.ofEpochMilli(value)!!

        @TypeConverter @JvmStatic
        fun toEpochMs(value: Instant) = value.toEpochMilli()

        @TypeConverter @JvmStatic
        fun toCode(value: CommentPage.Type) = value.code

        @TypeConverter @JvmStatic
        fun fromCode(value: Int) = CommentPage.Type.values().find { it.code == value }!!
    }
}
