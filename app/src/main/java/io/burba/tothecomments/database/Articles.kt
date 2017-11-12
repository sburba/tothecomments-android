package io.burba.tothecomments.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.burba.tothecomments.database.models.Article
import io.reactivex.Flowable

@Dao
interface Articles {
    @Query("SELECT * FROM articles ORDER BY last_search_time DESC")
    fun all(): Flowable<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun get(id: Long): Flowable<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(link: Article): Long
}
