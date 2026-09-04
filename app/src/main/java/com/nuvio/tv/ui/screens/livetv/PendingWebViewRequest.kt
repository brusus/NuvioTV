package com.nuvio.tv.ui.screens.livetv

/**
 * One-shot in-memory handoff from LiveTvScreen to the WebViewPlayer route. The
 * URL and title are also carried as nav-route arguments, but login credentials
 * are not - avoids putting a plaintext password into the nav back stack.
 */
object PendingWebViewRequest {
    @Volatile
    private var pending: LiveTvWebViewRequest? = null

    fun set(request: LiveTvWebViewRequest) {
        pending = request
    }

    fun consumeAndClear(): LiveTvWebViewRequest? {
        val request = pending
        pending = null
        return request
    }
}
