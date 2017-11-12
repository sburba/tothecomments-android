package io.burba.tothecomments.network.hackernews

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object HackerNews {
    private val retrofit = Retrofit.Builder()
            .baseUrl("http://hn.algolia.com/api/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()

    private val hn = retrofit.create(HnHttpService::class.java)

    fun loadComments(url: String): Single<List<HnCommentPage>> {
        return hn.getCommentPages(url).map { resp ->
            resp.hits.map { link ->
                HnCommentPage(link.title, "https://news.ycombinator.com/item?id=" + link.objectID)
            }
        }
    }
}