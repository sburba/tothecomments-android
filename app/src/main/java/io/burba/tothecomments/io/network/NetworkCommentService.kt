package io.burba.tothecomments.io.network

import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.burba.tothecomments.io.network.hackernews.NetworkHnService
import io.burba.tothecomments.io.network.reddit.NetworkRedditService
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles

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

    return Singles.zip(redditComments, hnComments, { reddit, hn -> reddit + hn })
}
