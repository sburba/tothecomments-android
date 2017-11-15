package io.burba.tothecomments.io.network.reddit

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

internal data class SearchResponse(val data: SearchResponseContent)
internal data class SearchResponseContent(val children: List<LinkWrapper>)
internal data class LinkWrapper(val data: Link)
internal data class Link(
        val title: String,
        val permalink: String,
        val subreddit: String
)

internal interface RedditHttpService {
    @GET("search.json?sort=comments&limit=5")
    fun search(@Query("q") query: String): Single<SearchResponse>
}
