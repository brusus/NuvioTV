package com.nuvio.tv.domain.model

/**
 * Common wrapper over the different live TV providers so the grid UI can render
 * one unified list. RAI channels resolve to a playable manifest via a scraper;
 * Mediaset channels are DRM-protected and are instead opened full-screen in a
 * WebView pointed at Mediaset's own authorized web player.
 */
sealed class LiveTvChannel {
    abstract val key: String
    abstract val displayName: String
    abstract val colorHex: String
    abstract val logoUrl: String?

    data class Rai(val channel: RaiLiveChannel) : LiveTvChannel() {
        override val key: String get() = "rai:${channel.slug}"
        override val displayName: String get() = channel.displayName
        override val colorHex: String get() = channel.colorHex
        override val logoUrl: String? get() = channel.logoUrl
    }

    data class Mediaset(val channel: MediasetLiveChannel) : LiveTvChannel() {
        override val key: String get() = "mediaset:${channel.slug}"
        override val displayName: String get() = channel.displayName
        override val colorHex: String get() = channel.colorHex
        override val logoUrl: String? get() = null
    }
}
