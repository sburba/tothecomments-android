package io.burba.tothecomments.ui.article

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsService
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso
import io.burba.tothecomments.R
import io.burba.tothecomments.io.database.models.Article
import io.burba.tothecomments.io.database.models.CommentPage
import io.burba.tothecomments.ui.afterSharedElementTransition
import io.burba.tothecomments.ui.show
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.android.synthetic.main.content_article.*

fun Activity.showComments(article: Article, articleImage: ImageView) {
    val intent = Intent(this, ArticleActivity::class.java)
    intent.putExtra(EXTRA_ARTICLE, article)
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, articleImage, "article_image")
    startActivity(intent, options.toBundle())
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

        // Hide the title and fab until after the shared element transition because they look funny
        // when the image is animating over them
        toolbar_layout.isTitleEnabled = false
        fab.visibility = View.GONE

        disposables += afterSharedElementTransition {
            toolbar_layout.isTitleEnabled = true
            fab.visibility = View.VISIBLE
        }

        adapter = CommentPageAdapter(this::launchCommentPage)
        layoutManager = LinearLayoutManager(this)

        comment_list_comments.setHasFixedSize(true)
        comment_list_comments.layoutManager = layoutManager
        comment_list_comments.adapter = adapter

        model = ViewModelProviders.of(this).get(ArticleViewModel::class.java)

        refresh_container.setOnRefreshListener {
            disposables += model.refresh().subscribe(this::setLoadingState)
        }
    }

    override fun onStart() {
        super.onStart()
        val state = model.getState(intent.sharedText, intent.article)
        disposables += state.contentStream.subscribe(this::setContentState)
        disposables += state.loading.subscribe(this::setLoadingState)
    }

    override fun onStop() {
        super.onStop()
        Picasso.with(this).cancelRequest(article_image)
        disposables.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            cleanupAndFinish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        cleanupAndFinish()
    }

    private fun cleanupAndFinish() {
        Picasso.with(this).cancelRequest(article_image)
        // The fab and toolbar text freak out in activity transitions, so hide them
        fab.visibility = View.GONE
        toolbar_layout.isTitleEnabled = false

        supportFinishAfterTransition()
    }

    private fun setLoadingState(loadingState: LoadingState) {
        when (loadingState) {
            LoadingState.LOADING -> refresh_container.isRefreshing = true
            LoadingState.LOADED -> refresh_container.isRefreshing = false
        }
    }

    private fun setContentState(state: ContentState) {
        when (state) {
            is ShowingContent -> {
                Picasso.with(this).load(state.article.imageUrl).into(article_image)
                toolbar_layout.title = state.article.title
                adapter.commentPages = state.comments
                comment_list_view_switcher.show(comment_list_comments)
            }
            is NoCommentsFoundError -> {
                toolbar_layout.title = state.article.title
                adapter.commentPages = arrayListOf()
                comment_list_error_message.text = getString(R.string.no_comments_found)
                comment_list_view_switcher.show(comment_list_error_message)
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

        if (intent.shouldUseCustomTabs(packageManager)) {
            CustomTabsIntent.Builder().build().launchUrl(this, uri)
        } else {
            startActivity(intent)
        }
    }
}

private const val EXTRA_ARTICLE = "com.burba.io.extra.article"
private const val EXTRA_URL = "com.burba.io.extra.url"

/**
 * Returns true of the current intent resolves to a package that supports custom tabs
 *
 * This allows you to use custom tabs if for example Chrome is the default opener for the intent,
 * but skip them if they have a custom app set up for it that doesn't support custom tabs (Like the
 * official reddit app)
 */
private fun Intent.shouldUseCustomTabs(packageManager: PackageManager): Boolean {
    val activity = this.resolveActivity(packageManager)

    val customTabsService = Intent().apply {
        action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
        `package` = activity.packageName
    }
    return packageManager.resolveService(customTabsService, 0) != null
}

private fun Intent.isTextShare() = this.action == Intent.ACTION_SEND && this.type == "text/plain"
private val Intent.sharedText: String?
    get() = if (isTextShare()) this.getStringExtra(Intent.EXTRA_TEXT) else null

private val Intent.article: Article?
    get() = this.getParcelableExtra(EXTRA_ARTICLE)
