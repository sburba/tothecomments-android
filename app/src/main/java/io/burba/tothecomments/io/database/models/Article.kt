package io.burba.tothecomments.io.database.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Instant
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@Entity(tableName = "articles",
        indices = arrayOf(Index(
                value = "url",
                unique = true
        ))
)
@PaperParcel
data class Article(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val url: String,
        @ColumnInfo(name = "last_search_time")
        val lastSearchTime: Instant = Instant.now(),
        @ColumnInfo(name = "last_comment_fetch_time")
        val lastCommentFetchTime: Instant? = null
) : PaperParcelable {
    companion object {
        @JvmField
        val CREATOR = PaperParcelArticle.CREATOR
    }
}
