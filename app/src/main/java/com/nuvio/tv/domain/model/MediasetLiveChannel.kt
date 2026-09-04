package com.nuvio.tv.domain.model

/**
 * A Mediaset Infinity live TV channel. Mediaset streams are Widevine DRM protected,
 * so unlike RAI these are played by loading directUrl in a WebView (Mediaset's own
 * authorized web player) rather than resolving a raw manifest URL via a scraper.
 */
data class MediasetLiveChannel(
    val slug: String,
    val displayName: String,
    val colorHex: String,
    val directUrl: String
)

object MediasetLiveChannels {
    // Fetched live from mediasetinfinity.mediaset.it on 2026-09-04.
    val ALL: List<MediasetLiveChannel> = listOf(
        MediasetLiveChannel(
            slug = "canale5",
            displayName = "Canale 5",
            colorHex = "#E30613",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/canale5_cC5"
        ),
        MediasetLiveChannel(
            slug = "italia1",
            displayName = "Italia 1",
            colorHex = "#0066B3",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/italia1_cI1"
        ),
        MediasetLiveChannel(
            slug = "rete4",
            displayName = "Rete 4",
            colorHex = "#6E2585",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/rete4_cR4"
        ),
        MediasetLiveChannel(
            slug = "20mediaset",
            displayName = "20 Mediaset",
            colorHex = "#F39200",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/20mediaset_cLB"
        ),
        MediasetLiveChannel(
            slug = "italia2",
            displayName = "Italia 2",
            colorHex = "#00A651",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/italia2_cI2"
        ),
        MediasetLiveChannel(
            slug = "iris",
            displayName = "Iris",
            colorHex = "#1D8348",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/iris_cKI"
        ),
        MediasetLiveChannel(
            slug = "la5",
            displayName = "La5",
            colorHex = "#D6006C",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/la5_cKA"
        ),
        MediasetLiveChannel(
            slug = "focus",
            displayName = "Focus",
            colorHex = "#00659E",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/focus_cFU"
        ),
        MediasetLiveChannel(
            slug = "topcrime",
            displayName = "Top Crime",
            colorHex = "#1A1A1A",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/topcrime_cLT"
        ),
        MediasetLiveChannel(
            slug = "cine34",
            displayName = "Cine34",
            colorHex = "#B8860B",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/cine34_cB6"
        ),
        MediasetLiveChannel(
            slug = "mediasetextra",
            displayName = "Mediaset Extra",
            colorHex = "#C8102E",
            directUrl = "https://mediasetinfinity.mediaset.it/diretta/mediasetextra_cKQ"
        )
    )
}
