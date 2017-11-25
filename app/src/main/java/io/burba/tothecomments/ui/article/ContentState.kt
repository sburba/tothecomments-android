package io.burba.tothecomments.ui.article

import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.reactivex.Flowable

data class ArticleActivityState(val contentStream: Flowable<out ContentState>, val loading: Flowable<LoadingState>)

enum class LoadingState {
    LOADING, LOADED
}

sealed class ContentState

data class ShowingContent(val article: Article, val comments: List<CommentPage>) : ContentState()
object UnknownError : ContentState()
data class InvalidUrlError(val url: String) : ContentState()
