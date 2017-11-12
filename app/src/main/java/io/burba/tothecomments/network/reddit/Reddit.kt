package io.burba.tothecomments.network.reddit

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object Reddit {
    private val retrofit = Retrofit.Builder()
            .baseUrl("https://www.reddit.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()

    private val reddit = retrofit.create(RedditHttpService::class.java)

    fun loadComments(url: String): Single<List<RedditCommentPage>> {
        return reddit.search("url:$url").map {
            it.data.children.map {
                RedditCommentPage(it.data.title, "https://reddit.com" + it.data.permalink)
            }
        }
    }
}