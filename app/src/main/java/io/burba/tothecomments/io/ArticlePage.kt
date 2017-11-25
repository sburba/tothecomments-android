package io.burba.tothecomments.io

import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage

data class ArticlePage(val article: Article, val comments: List<CommentPage>)