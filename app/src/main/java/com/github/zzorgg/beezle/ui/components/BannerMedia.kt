package com.github.zzorgg.beezle.ui.components

sealed class BannerMedia {
    data class RemoteImage(val url: String): BannerMedia()
    data class AssetGif(val assetFile: String): BannerMedia() // e.g. "Maths.gif"
    data class AssetVideo(val assetFile: String, val autoplay: Boolean = true, val loop: Boolean = true): BannerMedia()
}

