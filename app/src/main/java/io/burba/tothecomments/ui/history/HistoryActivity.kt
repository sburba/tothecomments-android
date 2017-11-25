package io.burba.tothecomments.ui.history

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import io.burba.tothecomments.R
import io.burba.tothecomments.io.ArticleService
import io.burba.tothecomments.ui.article.showComments
import io.burba.tothecomments.ui.ui
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.content_history.*

val urls = arrayOf("https://github.com/square/moshi/", "https://github.com/square/retrofit", "http://example.com")

class HistoryActivity : AppCompatActivity() {
    private val articleService by lazy { ArticleService.getInstance(this) }
    private val disposables = CompositeDisposable()
    private lateinit var adapter: ArticleAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(toolbar)

        adapter = ArticleAdapter(onItemClick = { showComments(it) })
        layoutManager = LinearLayoutManager(this)

        article_list.setHasFixedSize(true)
        article_list.layoutManager = layoutManager
        article_list.adapter = adapter
    }

    override fun onStart() {
        super.onStart()

        disposables.add(articleService.articles()
                .ui()
                .subscribe {
                    adapter.articles = it
                    // We just added an article on the top, show it
                    layoutManager.scrollToPosition(0)
                }
        )

        fab.setOnClickListener {
            val url = urls[(Math.random() * urls.size).toInt()]
            disposables += articleService.articlePage(url).ui().subscribe()
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}
