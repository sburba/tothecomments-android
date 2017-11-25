package io.burba.tothecomments.io.network.urlmeta

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

data class ArticleMeta(val title: String, val imageUrl: String?, val description: String?)
object NetworkUrlMetaService {
    private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.urlmeta.org")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()

    private val urlMeta = retrofit.create(UrlMetaApi::class.java)

    fun loadArticleMeta(url: String): Single<ArticleMeta> {
        return urlMeta.get(url).map { it.meta }.map {
            ArticleMeta(it.title ?: url, it.image ?: it.logo, it.description)
        }
    }
}