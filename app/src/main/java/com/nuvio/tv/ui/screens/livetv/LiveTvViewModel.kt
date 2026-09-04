package com.nuvio.tv.ui.screens.livetv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuvio.tv.core.plugin.PluginManager
import com.nuvio.tv.domain.model.LiveTvChannel
import com.nuvio.tv.domain.model.MediasetLiveChannels
import com.nuvio.tv.domain.model.RaiLiveChannels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RAIPLAY_LIVE_SCRAPER_ID = "official-raiplay-live"
private const val MEDIASET_LIVE_SCRAPER_ID = "official-mediaset-live"

data class LiveTvPlaybackRequest(
    val streamUrl: String,
    val headers: Map<String, String>?,
    val title: String
)

data class LiveTvWebViewRequest(
    val url: String,
    val title: String,
    val loginEmail: String? = null,
    val loginPassword: String? = null
)

data class LiveTvUiState(
    val resolvingChannelKey: String? = null,
    val error: LiveTvError? = null,
    val playbackRequest: LiveTvPlaybackRequest? = null,
    val webViewRequest: LiveTvWebViewRequest? = null
)

enum class LiveTvError {
    PROVIDER_NOT_INSTALLED,
    UNAVAILABLE
}

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val pluginManager: PluginManager
) : ViewModel() {

    val channels: List<LiveTvChannel> =
        RaiLiveChannels.ALL.map { LiveTvChannel.Rai(it) } +
            MediasetLiveChannels.ALL.map { LiveTvChannel.Mediaset(it) }

    private val _uiState = MutableStateFlow(LiveTvUiState())
    val uiState: StateFlow<LiveTvUiState> = _uiState.asStateFlow()

    fun playChannel(channel: LiveTvChannel) {
        when (channel) {
            is LiveTvChannel.Rai -> playRaiChannel(channel)
            is LiveTvChannel.Mediaset -> openMediasetChannel(channel)
        }
    }

    private fun openMediasetChannel(channel: LiveTvChannel.Mediaset) {
        viewModelScope.launch {
            // Stored scraper ids are namespaced as "<repositoryId>:<manifestScraperId>",
            // so match on the suffix rather than the bare manifest id.
            val scraper = pluginManager.scrapers.first()
                .firstOrNull { it.id.substringAfter(':') == MEDIASET_LIVE_SCRAPER_ID }
            val settings = scraper?.let { pluginManager.getScraperSettings(it.id) }
            val email = (settings?.get("email") as? String)?.takeIf { it.isNotBlank() }
            val password = (settings?.get("password") as? String)?.takeIf { it.isNotBlank() }
            _uiState.update {
                it.copy(
                    webViewRequest = LiveTvWebViewRequest(
                        url = channel.channel.directUrl,
                        title = channel.channel.displayName,
                        loginEmail = email,
                        loginPassword = password
                    )
                )
            }
        }
    }

    private fun playRaiChannel(channel: LiveTvChannel.Rai) {
        if (_uiState.value.resolvingChannelKey != null) return
        _uiState.update { it.copy(resolvingChannelKey = channel.key, error = null) }
        viewModelScope.launch {
            // Stored scraper ids are namespaced as "<repositoryId>:<manifestScraperId>",
            // so match on the suffix rather than the bare manifest id.
            val scraper = pluginManager.scrapers.first()
                .firstOrNull { it.id.substringAfter(':') == RAIPLAY_LIVE_SCRAPER_ID }
            if (scraper == null) {
                _uiState.update {
                    it.copy(resolvingChannelKey = null, error = LiveTvError.PROVIDER_NOT_INSTALLED)
                }
                return@launch
            }
            val results = pluginManager.executeScraper(
                scraper = scraper,
                tmdbId = "raitv:${channel.channel.slug}",
                mediaType = "tv",
                season = null,
                episode = null
            )
            val stream = results.firstOrNull()
            if (stream == null) {
                _uiState.update { it.copy(resolvingChannelKey = null, error = LiveTvError.UNAVAILABLE) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    resolvingChannelKey = null,
                    playbackRequest = LiveTvPlaybackRequest(
                        streamUrl = stream.url,
                        headers = stream.headers,
                        title = channel.channel.displayName
                    )
                )
            }
        }
    }

    fun consumePlaybackRequest() {
        _uiState.update { it.copy(playbackRequest = null) }
    }

    fun consumeWebViewRequest() {
        _uiState.update { it.copy(webViewRequest = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}
