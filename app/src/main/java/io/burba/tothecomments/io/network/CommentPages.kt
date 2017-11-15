package io.burba.tothecomments.io.network

import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.burba.tothecomments.io.network.hackernews.HackerNews
import io.burba.tothecomments.io.network.reddit.Reddit
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles

fun loadComments(article: Article): Single<List<CommentPage>> {
    val redditComments = Reddit.loadComments(article.url).map {
        it.map {
            CommentPage(0, article.id, it.title, it.url, CommentPage.Type.REDDIT)
        }
    }

    val hnComments = HackerNews.loadComments(article.url).map {
        it.map {
            CommentPage(0, article.id, it.title, it.url, CommentPage.Type.HN)
        }
    }

    return Singles.zip(redditComments, hnComments, { reddit, hn -> reddit + hn })
}
