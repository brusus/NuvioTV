package com.nuvio.tv.ui.screens.livetv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuvio.tv.core.plugin.PluginManager
import com.nuvio.tv.domain.model.RaiLiveChannel
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

data class LiveTvPlaybackRequest(
    val streamUrl: String,
    val headers: Map<String, String>?,
    val title: String
)

data class LiveTvUiState(
    val resolvingChannelSlug: String? = null,
    val error: LiveTvError? = null,
    val playbackRequest: LiveTvPlaybackRequest? = null
)

enum class LiveTvError {
    PROVIDER_NOT_INSTALLED,
    UNAVAILABLE
}

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val pluginManager: PluginManager
) : ViewModel() {

    val channels: List<RaiLiveChannel> = RaiLiveChannels.ALL

    private val _uiState = MutableStateFlow(LiveTvUiState())
    val uiState: StateFlow<LiveTvUiState> = _uiState.asStateFlow()

    fun playChannel(channel: RaiLiveChannel) {
        if (_uiState.value.resolvingChannelSlug != null) return
        _uiState.update { it.copy(resolvingChannelSlug = channel.slug, error = null) }
        viewModelScope.launch {
            // Stored scraper ids are namespaced as "<repositoryId>:<manifestScraperId>",
            // so match on the suffix rather than the bare manifest id.
            val scraper = pluginManager.scrapers.first()
                .firstOrNull { it.id.substringAfter(':') == RAIPLAY_LIVE_SCRAPER_ID }
            if (scraper == null) {
                _uiState.update {
                    it.copy(resolvingChannelSlug = null, error = LiveTvError.PROVIDER_NOT_INSTALLED)
                }
                return@launch
            }
            val results = pluginManager.executeScraper(
                scraper = scraper,
                tmdbId = "raitv:${channel.slug}",
                mediaType = "tv",
                season = null,
                episode = null
            )
            val stream = results.firstOrNull()
            if (stream == null) {
                _uiState.update { it.copy(resolvingChannelSlug = null, error = LiveTvError.UNAVAILABLE) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    resolvingChannelSlug = null,
                    playbackRequest = LiveTvPlaybackRequest(
                        streamUrl = stream.url,
                        headers = stream.headers,
                        title = channel.displayName
                    )
                )
            }
        }
    }

    fun consumePlaybackRequest() {
        _uiState.update { it.copy(playbackRequest = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}
