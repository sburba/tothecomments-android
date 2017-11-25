package io.burba.tothecomments.io.database.util

import android.arch.persistence.room.TypeConverter
import io.burba.tothecomments.io.database.models.CommentPage
import org.threeten.bp.Instant

class DbConverters {
    companion object {
        @TypeConverter @JvmStatic
        fun fromEpochMs(value: Long?) = value?.let { Instant.ofEpochMilli(value) }

        @TypeConverter @JvmStatic
        fun toEpochMs(value: Instant?) = value?.toEpochMilli()

        @TypeConverter @JvmStatic
        fun toCode(value: CommentPage.Type) = value.code

        @TypeConverter @JvmStatic
        fun fromCode(value: Int) = CommentPage.Type.values().find { it.code == value }!!
    }
}
