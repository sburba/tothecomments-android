package io.burba.tothecomments.io.database.models

import android.arch.persistence.room.*

@Entity(tableName = "comment_pages",
        indices = arrayOf(Index("article_id")),
        foreignKeys = arrayOf(ForeignKey(
                entity = Article::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("article_id"),
                onDelete = ForeignKey.CASCADE)))
data class CommentPage(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "article_id") val articleId: Long,
        val title: String,
        val url: String,
        val type: Type
) {
    enum class Type(val code: Int) { HN(0), REDDIT(1) }
}

