package io.burba.tothecomments.io.network.hackernews

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

internal data class SearchResponse(val hits: List<Link>)
internal data class Link(val title: String, val objectID: String)

internal interface HnApi {
    @GET("search?restrictSearchableAttributes=url&hitsPerPage=5")
    fun getCommentPages(@Query("query") url: String): Single<SearchResponse>
}
