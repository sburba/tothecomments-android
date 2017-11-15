package io.burba.tothecomments.ui.article

import io.burba.tothecomments.database.models.Article
import io.burba.tothecomments.database.models.CommentPage

sealed class ArticleActivityState

data class Loading(val url: String) : ArticleActivityState()
data class LoadingWithArticle(val article: Article) : ArticleActivityState()
data class Loaded(val article: Article, val commentPages: List<CommentPage>) : ArticleActivityState()
object UnknownError : ArticleActivityState()
data class InvalidUrlError(val url: String) : ArticleActivityState()
