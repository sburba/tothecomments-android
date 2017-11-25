package io.burba.tothecomments.io.database.util

import android.os.Parcel
import org.threeten.bp.Instant
import paperparcel.TypeAdapter

object InstantAdapter : TypeAdapter<Instant> {
    override fun writeToParcel(value: Instant, dest: Parcel, flags: Int) {
        dest.writeLong(value.toEpochMilli())
    }

    override fun readFromParcel(source: Parcel) = Instant.ofEpochMilli(source.readLong())!!
}
