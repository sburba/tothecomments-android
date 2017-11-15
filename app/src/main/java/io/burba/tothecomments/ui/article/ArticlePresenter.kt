package io.burba.tothecomments.ui.article

import android.util.Patterns
import io.burba.tothecomments.io.database.Db
import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.burba.tothecomments.io.network.loadComments
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.subscribeBy


class ArticlePresenter(private val activity: ArticleActivity) {
    private val db by lazy { Db.getInstance(activity.applicationContext) }
    private val disposables = CompositeDisposable()
    private var stateStream: Flowable<out ArticleActivityState>? = null

    fun onStart(sharedText: String?, articleId: Long?) {
        stateStream = when {
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
        }

        stateStream?.let {
            disposables.add(it.subscribeBy(
                    onNext = { activity.setState(it) },
                    onError = { activity.setState(UnknownError) }
            ))
        }
    }

    fun onStop() {
        disposables.clear()
    }

    fun onRefreshRequested() {
        stateStream?.subscribeBy(
                onNext = { activity.setState(it) },
                onError = { activity.setState(UnknownError) }
        ) ?: activity.setState(UnknownError)
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
            Loaded(it.article, it.comments)
        }
    }
}

private data class ArticleWithComments(val article: Article, val comments: List<CommentPage>)

private fun String.isUrl() = Patterns.WEB_URL.matcher(this).matches()
