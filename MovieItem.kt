package com.sandip.mymovie

data class MovieItem(
    val title: String,
    val poster: String,
    val category: String,
    val embedLink: String,
    val downloadLink: String
) {
    // असली प्लेबैक के लिए: पहले DownloadLink, वो खाली/# हो तो EmbedLink
    fun playableLink(): String {
        return if (downloadLink.isNotBlank() && downloadLink != "#") downloadLink else embedLink
    }
}
