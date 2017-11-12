package io.burba.tothecomments.database.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Instant
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
@Entity(tableName = "articles",
        indices = arrayOf(Index(
                value = "url",
                unique = true
        ))
)
data class Article(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val url: String,
        @ColumnInfo(name = "last_search_time")
        val lastSearchTime: Instant = Instant.now()
) : PaperParcelable {
    // Parcelable makes me so sad
    companion object {
        @JvmField
        val CREATOR = PaperParcelArticle.CREATOR
    }
}
