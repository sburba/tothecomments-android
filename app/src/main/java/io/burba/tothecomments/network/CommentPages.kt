package io.burba.tothecomments.network

import io.burba.tothecomments.database.models.Article
import io.burba.tothecomments.database.models.CommentPage
import io.burba.tothecomments.network.reddit.Reddit
import io.reactivex.Single

fun loadComments(article: Article): Single<List<CommentPage>> {
    return Reddit.loadComments(article.url).map {
        it.map {
            CommentPage(0, article.id, it.title, it.url, CommentPage.Type.REDDIT)
        }
    }
}
