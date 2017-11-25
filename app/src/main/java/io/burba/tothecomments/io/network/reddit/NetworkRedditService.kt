package io.burba.tothecomments.io.network.reddit

import io.burba.tothecomments.BuildConfig
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private const val USER_AGENT = "android:io.burba.tothecomments:v${BuildConfig.VERSION_NAME} (by /u/thisnameistoolon)"

object NetworkRedditService {
    private val httpClient = OkHttpClient().newBuilder().addInterceptor({ chain ->
        chain.proceed(
                chain.request()
                        .newBuilder()
                        .addHeader("User-Agent", USER_AGENT)
                        .build()
        )
    }).build()

    private val retrofit = Retrofit.Builder()
            .baseUrl("https://www.reddit.com/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()

    private val reddit = retrofit.create(RedditApi::class.java)

    fun loadComments(url: String): Single<List<RedditCommentPage>> {
        return reddit.search("url:$url").map {
            it.data.children.map {
                RedditCommentPage(it.data.title, "https://reddit.com" + it.data.permalink)
            }
        }
    }
}