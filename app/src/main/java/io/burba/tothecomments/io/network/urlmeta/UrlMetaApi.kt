package io.burba.tothecomments.io.network.urlmeta

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

internal data class Response(val meta: MetaInfo)
internal data class MetaInfo(
        val title: String?,
        val description: String?,
        val image: String?,
        val logo: String?
)

internal interface UrlMetaApi {
    @GET("/")
    fun get(@Query("url") url: String): Single<Response>
}