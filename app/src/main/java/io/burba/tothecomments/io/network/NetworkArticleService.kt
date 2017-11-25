package io.burba.tothecomments.io.network

import android.util.Log
import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.burba.tothecomments.io.network.hackernews.NetworkHnService
import io.burba.tothecomments.io.network.reddit.NetworkRedditService
import io.burba.tothecomments.io.network.urlmeta.NetworkUrlMetaService
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles

fun loadArticle(url: String): Single<Article> {
    return NetworkUrlMetaService.loadArticleMeta(url).map {
        Article(0, url, it.title, it.imageUrl)
    }.onErrorReturn {
        Article(0, url, url)
    }
}

fun loadComments(article: Article): Single<List<CommentPage>> {
    val redditComments = NetworkRedditService.loadComments(article.url).map {
        it.map {
            CommentPage(0, article.id, it.title, it.url, CommentPage.Type.REDDIT)
        }
    }

    val hnComments = NetworkHnService.loadComments(article.url).map {
        it.map {
            CommentPage(0, article.id, it.title, it.url, CommentPage.Type.HN)
        }
    }

    Log.d("Network", "loadComments called")
    return Singles.zip(redditComments, hnComments, { reddit, hn -> reddit + hn })
}
