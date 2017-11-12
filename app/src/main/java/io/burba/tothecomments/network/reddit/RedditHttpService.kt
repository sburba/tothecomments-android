package io.burba.tothecomments.network.reddit

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

internal interface RedditHttpService {
    // This is a stupid hack
    @GET("search.json?sort=comments&limit=5")
    fun search(@Query("q") query: String): Single<SearchResponse>
}
