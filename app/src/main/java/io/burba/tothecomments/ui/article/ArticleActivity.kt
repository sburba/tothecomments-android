package io.burba.tothecomments.ui.article

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import io.burba.tothecomments.R
import io.burba.tothecomments.io.database.models.CommentPage
import io.burba.tothecomments.ui.show
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

    private val disposables = CompositeDisposable()
    private lateinit var model: ArticleViewModel
    private lateinit var adapter: CommentPageAdapter
    private lateinit var layoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        // So, if you set the title to null or empty string, it shows as To The Comments!
        // But if you set it to a space character, it will actually stay blank
        // This makes me sad
        toolbar_layout.title = " "

        adapter = CommentPageAdapter(this::launchCommentPage)
        layoutManager = LinearLayoutManager(this)

        comment_list_comments.setHasFixedSize(true)
        comment_list_comments.layoutManager = layoutManager
        comment_list_comments.adapter = adapter

        model = ViewModelProviders.of(this).get(ArticleViewModel::class.java)

        refresh_container.setOnRefreshListener {
            disposables.clear()
            disposables.add(model.refresh().subscribe(this::setState))
        }
    }

    override fun onStart() {
        super.onStart()
        disposables.add(
                model.getState(intent.sharedText, intent.articleId)
                        .subscribe(this::setState)
        )
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    private fun setState(state: ArticleActivityState) {
        when (state) {
            is Loading -> {
                toolbar_layout.title = state.url
                refresh_container.isRefreshing = true
                comment_list_view_switcher.show(comment_list_comments)
            }
            is LoadingWithArticle -> {
                toolbar_layout.title = state.article.url
                refresh_container.isRefreshing = true
                comment_list_view_switcher.show(comment_list_comments)
            }
            is Loaded -> {
                toolbar_layout.title = state.article.url
                refresh_container.isRefreshing = false
                adapter.commentPages = state.commentPages
                comment_list_view_switcher.show(comment_list_comments)
            }
            is InvalidUrlError -> {
                adapter.clear()
                toolbar_layout.title = getString(R.string.ya_goof)
                refresh_container.isRefreshing = false
                comment_list_error_message.text = getString(R.string.invalid_url, state.url)
                comment_list_view_switcher.show(comment_list_error_message)
            }
            is UnknownError -> {
                adapter.clear()
                toolbar_layout.title = getString(R.string.error)
                refresh_container.isRefreshing = false
                comment_list_error_message.text = getString(R.string.load_comments_failed)
                comment_list_view_switcher.show(comment_list_error_message)
            }
        }
    }

    private fun launchCommentPage(page: CommentPage) {
        val uri = Uri.parse(page.url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val activity = intent.resolveActivity(packageManager)

        /**
         * This is a silly little hack.
         *
         * If the user has a custom app to handle the uri (like the official NetworkRedditService app) they will
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

private fun Intent.isTextShare() = this.action == Intent.ACTION_SEND && this.type == "text/plain"
private val Intent.sharedText: String?
    get() = if (isTextShare()) this.getStringExtra(Intent.EXTRA_TEXT) else null

private val Intent.articleId: Long?
    get() {
        val id = this.getLongExtra(EXTRA_ARTICLE_ID, -1)
        return if (id != -1L) id else null
    }
