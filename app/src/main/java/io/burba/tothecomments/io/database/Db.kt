package io.burba.tothecomments.io.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room.databaseBuilder
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.burba.tothecomments.io.database.util.Converters
import io.burba.tothecomments.util.SingletonHolder

@Database(entities = arrayOf(Article::class, CommentPage::class), version = 1)
@TypeConverters(Converters::class)
abstract class Db : RoomDatabase() {
    abstract fun articles(): Articles

    companion object : SingletonHolder<Db, Context>({ context ->
        databaseBuilder(context.applicationContext, Db::class.java, "to_the_comments")
                .fallbackToDestructiveMigration()
                .build()
    })
}