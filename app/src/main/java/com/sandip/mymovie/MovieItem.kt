package com.sandip.mymovie

data class MovieItem(
    val title: String,
    val poster: String,
    val category: String,
    val embedLink: String,
    val downloadLink: String
) {
    fun playableLink(): String {
        return if (downloadLink.isNotBlank() && downloadLink != "#") downloadLink else embedLink
    }
}
