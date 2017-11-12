package io.burba.tothecomments.network.reddit

// data.children.map( child -> { title: child.data.title, link: child.data.permalink, subreddit: child.data.subreddit } }

data class SearchResponse(val data: SearchResponseContent)
data class SearchResponseContent(val children: List<LinkWrapper>)
data class LinkWrapper(val data: Link)
data class Link(
        val title: String,
        val permalink: String,
        val subreddit: String
)
