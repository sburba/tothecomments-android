package io.burba.tothecomments.ui.history

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.burba.tothecomments.database.models.Article

class ArticleAdapter(
        articles: List<Article> = arrayListOf(),
        private val onItemClick: (Article) -> (Unit) = {}
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    var articles: List<Article> = articles
        set(value) {
            val diff = DiffUtil.calculateDiff(DiffCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun getItemCount() = articles.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]

        holder.textView.text = article.url
    }

    fun onItemClick(pos: Int) {
        onItemClick(articles[pos])
    }

    inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView), View.OnClickListener {
        override fun onClick(v: View) {
            onItemClick(adapterPosition)
        }

        init {
            textView.setOnClickListener(this)
        }
    }

    private class DiffCallback(private val old: List<Article>, private val new: List<Article>) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int) = old[oldPos].url == new[newPos].url
        override fun areContentsTheSame(oldPos: Int, newPos: Int) = old[oldPos] == new[newPos]
    }
}