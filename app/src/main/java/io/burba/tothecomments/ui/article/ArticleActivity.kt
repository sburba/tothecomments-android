package io.burba.tothecomments.ui.article

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Patterns
import android.widget.Toast
import io.burba.tothecomments.R
import io.burba.tothecomments.database.Db
import io.burba.tothecomments.database.models.Article
import io.burba.tothecomments.database.models.CommentPage
import io.burba.tothecomments.network.loadComments
import io.burba.tothecomments.ui.ui
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.android.synthetic.main.content_article.*

fun Activity.showComments(articleId: Long) {
    val intent = Intent(this, ArticleActivity::class.java)
    intent.putExtra(EXTRA_ARTICLE_ID, articleId)
    startActivity(intent)
}

fun Activity.showComments(url: String) {
    val intent = Intent(this, ArticleActivity::class.java)
    intent.putExtra(EXTRA_URL, url)
    startActivity(intent)
}

class ArticleActivity : AppCompatActivity() {
    private val db by lazy { Db.getInstance(this) }

    private val disposables = CompositeDisposable()
    private lateinit var adapter: CommentPageAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        adapter = CommentPageAdapter(this::launchCommentPage)
        layoutManager = LinearLayoutManager(this)

        comment_page_list.setHasFixedSize(true)
        comment_page_list.layoutManager = layoutManager
        comment_page_list.adapter = adapter
    }

    override fun onStart() {
        super.onStart()

        val articleIdFlowable: Flowable<Long>? = when {
            intent.sharedUrl != null -> Flowable.fromCallable { db.articles().add(Article(0, intent.sharedUrl!!)) }
            intent.articleId != null -> Flowable.just(intent.articleId)
            else -> null
        }

        when {
            articleIdFlowable != null -> loadArticle(articleIdFlowable)
            intent.isTextShare() -> {
                Toast.makeText(this, "Unable to recognize ${intent.sharedText} as url", Toast.LENGTH_SHORT).show()
                finish()
            }
            else -> {
                Toast.makeText(this, "I've failed you, senpai", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    private fun loadArticle(articleIdFlowable: Flowable<Long>) {
        disposables.add(
                articleIdFlowable.flatMap {
                    db.articles().get(it)
                }.ui().doOnNext {
                    toolbar_layout.title = it.url
                    fab.setOnClickListener { view ->
                        Snackbar.make(view, "Sharing $it.url (Not Really)", Snackbar.LENGTH_LONG).show()
                    }
                }.flatMap {
                    loadComments(it).toFlowable()
                }.ui().subscribe { comments ->
                    adapter.commentPages = comments
                }
        )
    }

    private fun launchCommentPage(page: CommentPage) {
        val uri = Uri.parse(page.url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val activity = intent.resolveActivity(packageManager)

        /**
         * This is a silly little hack.
         *
         * If the user has a custom app to handle the uri (like the official Reddit app) they will
         * probably want to open the link in that app instead of chrome custom tabs
         *
         * So, if this url resolves to something other than chrome, we just launch it normally.
         *
         * Now, you're saying, what if they just have a different default browser? Well, as it turns
         * out, if chrome detects that another browser is default it won't use custom tabs and it
         * will launch their preferred browser anyways. So this hack should have the same result
         */
        if (activity.className != CHROME_PACKAGE_NAME) {
            startActivity(intent)
        } else {
            CustomTabsIntent.Builder().build().launchUrl(this, uri)
        }
    }
}

private const val EXTRA_ARTICLE_ID = "com.burba.io.extra.article"
private const val EXTRA_URL = "com.burba.io.extra.url"
private const val CHROME_PACKAGE_NAME = "com.google.android.apps.chrome.Main"

private fun String.isUrl() = Patterns.WEB_URL.matcher(this).matches()
private fun Intent.isTextShare() = this.action == Intent.ACTION_SEND && this.type == "text/plain"
private val Intent.sharedText: String?
    get() = this.getStringExtra(Intent.EXTRA_TEXT)

private val Intent.sharedUrl: String?
    get() = if (this.isTextShare() && sharedText?.isUrl() == true) {
        sharedText
    } else {
        null
    }

private val Intent.articleId: Long?
    get() {
        val id = this.getLongExtra(EXTRA_ARTICLE_ID, -1)
        return if (id != -1L) id else null
    }
