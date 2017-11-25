package io.burba.tothecomments.io

import android.content.Context
import io.burba.tothecomments.io.database.Db
import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.network.loadArticle as loadNetworkArticle
import io.burba.tothecomments.util.SingletonHolder
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import org.threeten.bp.Instant
import io.burba.tothecomments.io.network.loadComments as loadNetworkComments

class ArticleService(context: Context) {
    private val dbArticles = Db.getInstance(context).articles()

    fun articles(): Flowable<List<Article>> = dbArticles.all()

    fun articlePage(article: Article): Flowable<ArticlePage> = Flowable.just(article).articlePage()

    fun articlePage(url: String): Flowable<ArticlePage> {
        return loadNetworkArticle(url).toFlowable().flatMap {
            val articleId = dbArticles.add(it)
            dbArticles.get(articleId)
        }.articlePage()
    }

    fun refreshComments(article: Article) = loadNetworkComments(article).map {
        dbArticles.recordCommentsFetched(article.id, Instant.now())
        dbArticles.addAll(it)
    }!!

    private fun Flowable<Article>.articlePage(): Flowable<ArticlePage> {
        return this.flatMap { article ->
            val comments = if (article.lastCommentFetchTime == null) {
                refreshComments(article).toFlowable().flatMap { dbArticles.getComments(article.id) }
            } else {
                dbArticles.getComments(article.id)
            }

            Flowables.zip(Flowable.just(article), comments, ::ArticlePage)
        }
    }

    companion object : SingletonHolder<ArticleService, Context>({ context ->
        ArticleService(context.applicationContext)
    })
}
