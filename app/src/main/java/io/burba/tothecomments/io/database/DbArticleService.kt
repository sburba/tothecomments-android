package io.burba.tothecomments.io.database

import android.arch.persistence.room.*
import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.reactivex.Flowable
import org.threeten.bp.Instant

@Dao
interface DbArticleService {
    @Query("SELECT * FROM articles ORDER BY last_search_time DESC")
    fun all(): Flowable<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun get(id: Long): Flowable<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(link: Article): Long

    @Query("SELECT * FROM comment_pages WHERE article_id = :articleId")
    fun getComments(articleId: Long): Flowable<List<CommentPage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAll(commentPages: List<CommentPage>)

    @Query("UPDATE articles SET last_comment_fetch_time = :atInstant WHERE id=:articleId")
    fun recordCommentsFetched(articleId: Long, atInstant: Instant)
}
