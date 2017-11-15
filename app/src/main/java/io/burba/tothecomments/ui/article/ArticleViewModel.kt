package io.burba.tothecomments.ui.article

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.util.Patterns
import io.burba.tothecomments.io.database.Db
import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.burba.tothecomments.io.network.loadComments
import io.burba.tothecomments.ui.ui
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.rxkotlin.Flowables

class ArticleViewModel(app: Application) : AndroidViewModel(app) {
    private val db by lazy { Db.getInstance(app) }
    private var stateStream: ConnectableFlowable<out ArticleActivityState>? = null
    private var disposable: Disposable? = null

    fun getState(sharedText: String?, articleId: Long?): Flowable<out ArticleActivityState> {
        val state = stateStream ?: when {
            articleId != null -> {
                loadArticle(articleId)
            }
            sharedText != null && sharedText.isUrl() -> { // They've shared a valid url
                loadArticle(sharedText)
            }
            sharedText != null && !sharedText.isUrl() -> { // They've shared an invalid url
                Flowable.just(InvalidUrlError(sharedText))
            }
            else -> { // There's no article or url
                Flowable.just(UnknownError)
            }
        }.ui().replay(1)

        disposable = state.connect()
        stateStream = state
        return state
    }

    fun refresh(): Flowable<out ArticleActivityState> {
        return stateStream?.let {
            // Force the observable to reload by disposing the original connection and making a new one
            disposable?.dispose()
            disposable = it.connect()
            return it
        } ?: Flowable.just(UnknownError)
    }

    private fun loadArticle(url: String): Flowable<ArticleActivityState> {
        return loadArticle(Flowable.fromCallable {
            db.articles().add(Article(0, url))
        })
    }

    private fun loadArticle(id: Long): Flowable<ArticleActivityState> {
        return loadArticle(Flowable.just(id))
    }

    private fun loadArticle(articleIdFlowable: Flowable<Long>): Flowable<ArticleActivityState> {
        return articleIdFlowable.flatMap {
            db.articles().get(it)
        }.distinct {
            it.url
        }.flatMap { article ->
            Flowables.zip(Flowable.just(article), loadComments(article).toFlowable(), ::ArticleWithComments)
        }.map {
            if (it.comments.isEmpty()) NoCommentsFoundError(it.article) else Loaded(it.article, it.comments)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}

private data class ArticleWithComments(val article: Article, val comments: List<CommentPage>)

private fun String.isUrl() = Patterns.WEB_URL.matcher(this).matches()
