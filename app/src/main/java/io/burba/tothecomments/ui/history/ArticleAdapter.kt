package io.burba.tothecomments.ui.history

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.burba.tothecomments.R
import io.burba.tothecomments.io.database.models.Article

private const val IMAGE_ARTICLE = 1
private const val TEXT_ARTICLE = 2

class ArticleAdapter(
        articles: List<Article> = arrayListOf(),
        private val onItemClick: (Article, ImageView?) -> (Unit)
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    var articles: List<Article> = articles
        set(value) {
            val diff = DiffUtil.calculateDiff(DiffCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun getItemCount() = articles.size

    override fun getItemViewType(position: Int) = when {
        articles[position].imageUrl != null -> IMAGE_ARTICLE
        else -> TEXT_ARTICLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            IMAGE_ARTICLE -> inflater.inflate(R.layout.item_image_article, parent, false)
            TEXT_ARTICLE -> inflater.inflate(R.layout.item_text_article, parent, false)
            else -> throw IllegalArgumentException("Unexpected view type: $viewType")
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]

        holder.title.text = article.title
        if (holder.image != null) {
            Picasso.with(holder.image.context).load(article.imageUrl).into(holder.image)
        }
    }

    private fun handleItemClick(pos: Int, v: ImageView?) {
        onItemClick(articles[pos], v)
    }

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root), View.OnClickListener {
        val title = root.findViewById<TextView>(R.id.article_title)!!
        val image: ImageView? = root.findViewById(R.id.article_image)

        init {
            root.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            handleItemClick(adapterPosition, image)
        }
    }
}


private class DiffCallback(private val old: List<Article>, private val new: List<Article>) : DiffUtil.Callback() {
    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areItemsTheSame(oldPos: Int, newPos: Int) = old[oldPos].url == new[newPos].url
    override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            old[oldPos].title == new[newPos].title &&
                    old[oldPos].imageUrl == new[newPos].imageUrl
}
