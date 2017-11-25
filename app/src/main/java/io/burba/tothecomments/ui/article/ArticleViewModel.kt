package io.burba.tothecomments.ui.article

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.util.Patterns
import io.burba.tothecomments.io.ArticlePage
import io.burba.tothecomments.io.ArticleService
import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.ui.ui
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable

class ArticleViewModel(app: Application) : AndroidViewModel(app) {
    private val articleService by lazy { ArticleService.getInstance(app) }
    private var contentStateStream: ConnectableFlowable<out ContentState>? = null
    private var disposable: Disposable? = null

    fun getState(sharedText: String?, article: Article?): ArticleActivityState {
        val state = contentStateStream ?: when {
            article != null -> {
                articleService.articlePage(article).map(::toLoadedState)
            }
            sharedText != null -> {
                if (sharedText.isUrl()) {
                    articleService.articlePage(sharedText).map(::toLoadedState)
                } else {
                    Flowable.just(InvalidUrlError(sharedText))
                }
            }
            else -> {
                Flowable.just(UnknownError)
            }
        }.ui().replay(1)

        disposable = state.connect()
        contentStateStream = state
        return ArticleActivityState(state, loadingUntil(state.firstOrError()))
    }

    fun refresh(): Flowable<LoadingState> {
        return contentStateStream?.let {
            it.firstOrError().toFlowable().flatMap { state ->
                when (state) {
                    is ShowingContent -> loadingUntil(articleService.refreshComments(state.article))
                    else -> Flowable.just(LoadingState.LOADED)
                }
            }
        } ?: Flowable.just(LoadingState.LOADED)
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}

private fun String.isUrl() = Patterns.WEB_URL.matcher(this).matches()

private fun loadingUntil(loadedTrigger: Single<*>): Flowable<LoadingState> =
        Single.concat(Single.just(LoadingState.LOADED), loadedTrigger.map { LoadingState.LOADED })

private fun toLoadedState(articlePage: ArticlePage) =
        if (articlePage.comments.isEmpty())
            NoCommentsFoundError(articlePage.article)
        else
            ShowingContent(articlePage.article, articlePage.comments)
