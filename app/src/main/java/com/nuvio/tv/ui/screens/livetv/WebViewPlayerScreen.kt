package com.nuvio.tv.ui.screens.livetv

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.nuvio.tv.ui.components.LoadingIndicator

/**
 * Full-screen WebView player used for providers whose streams are DRM-protected
 * (e.g. Mediaset Infinity) and therefore can't be resolved into a raw manifest
 * URL. This loads the provider's own authorized web player directly - Android's
 * WebView is Chromium-based and negotiates Widevine EME itself, same as a real
 * Chrome visit would, with no DRM circumvention involved.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPlayerScreen(
    url: String,
    loginEmail: String? = null,
    loginPassword: String? = null,
    onExit: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    BackHandler {
        val callback = customViewCallback
        val webView = webViewRef
        when {
            callback != null -> callback.onCustomViewHidden()
            webView != null && webView.canGoBack() -> webView.goBack()
            else -> onExit()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val frame = FrameLayout(ctx)
                // Chromium's WebView has no built-in D-pad spatial navigation for
                // arbitrary web content, so arrow keys would otherwise do nothing (or
                // leak out to Compose and move focus off the WebView entirely). This
                // subclass intercepts D-pad keys and forwards them to a JS spatial-nav
                // layer injected on page load (see buildSpatialNavScript below).
                val webView = object : WebView(ctx) {
                    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                        val direction = when (event.keyCode) {
                            KeyEvent.KEYCODE_DPAD_UP -> "up"
                            KeyEvent.KEYCODE_DPAD_DOWN -> "down"
                            KeyEvent.KEYCODE_DPAD_LEFT -> "left"
                            KeyEvent.KEYCODE_DPAD_RIGHT -> "right"
                            else -> null
                        }
                        if (direction != null) {
                            if (event.action == KeyEvent.ACTION_DOWN) {
                                evaluateJavascript("window.__tvNav && window.__tvNav.move('$direction');", null)
                            }
                            return true
                        }
                        if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                            if (event.action == KeyEvent.ACTION_DOWN) {
                                evaluateJavascript("window.__tvNav && window.__tvNav.activate();", null)
                            }
                            return true
                        }
                        return super.dispatchKeyEvent(event)
                    }
                }.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Compose's AndroidView doesn't focus its child automatically, and
                    // without focus the D-pad has nothing to route key events to.
                    isFocusable = true
                    isFocusableInTouchMode = true
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    // Some sites gate playback behind a UA sniff that rejects the "; wv"
                    // WebView marker Chromium appends by default; strip it so the page
                    // renders its normal desktop-Chrome experience.
                    settings.userAgentString = settings.userAgentString.replace("; wv", "")
                }
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                        isLoading = false
                        view?.evaluateJavascript(SPATIAL_NAV_SCRIPT, null)
                        if (!loginEmail.isNullOrBlank() && !loginPassword.isNullOrBlank()) {
                            view?.evaluateJavascript(buildAutoFillScript(loginEmail, loginPassword), null)
                        }
                    }
                }
                webView.webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        if (view == null) return
                        customView = view
                        customViewCallback = callback
                        frame.addView(
                            view,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                        webView.visibility = View.GONE
                    }

                    override fun onHideCustomView() {
                        val view = customView ?: return
                        frame.removeView(view)
                        customView = null
                        customViewCallback = null
                        webView.visibility = View.VISIBLE
                    }
                }
                webView.loadUrl(url)
                webViewRef = webView
                frame.addView(webView)
                frame
            }
        )
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator(modifier = Modifier.fillMaxWidth(0.12f).aspectRatio(1f))
            }
        }
    }

    LaunchedEffect(webViewRef) {
        webViewRef?.requestFocus()
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                webChromeClient = null
                webViewClient = object : WebViewClient() {}
                destroy()
            }
        }
    }
}

/**
 * Chromium's WebView doesn't do spatial navigation between focusable DOM
 * elements the way a native TV UI does, so arrow keys need a JS-side stand-in:
 * find the visible focusable element nearest in the pressed direction, focus
 * it (with a forced outline, since many sites suppress the default one), and
 * scroll it into view. window.__tvNav.activate() clicks whatever is focused.
 * Re-injected on every onPageFinished but guards itself against double-init
 * so it's a no-op if the page hasn't actually navigated.
 */
private const val SPATIAL_NAV_SCRIPT = """
(function() {
    if (window.__tvNav) return;
    var style = document.createElement('style');
    style.textContent = ':focus { outline: 3px solid #ffffff !important; outline-offset: 2px !important; }';
    document.head.appendChild(style);

    function isVisible(el) {
        var r = el.getBoundingClientRect();
        if (r.width === 0 || r.height === 0) return false;
        var style = window.getComputedStyle(el);
        if (style.visibility === 'hidden' || style.display === 'none') return false;
        return r.bottom > 0 && r.right > 0 && r.top < window.innerHeight && r.left < window.innerWidth;
    }
    function isTopMost(el) {
        // Excludes elements that are geometrically on-screen but actually hidden
        // behind something else (e.g. a page's own content sitting under a cookie-
        // consent modal's backdrop) - without this, nav would "focus" targets the
        // user can't see or actually click.
        var r = el.getBoundingClientRect();
        var cx = Math.min(Math.max(r.left + r.width / 2, 0), window.innerWidth - 1);
        var cy = Math.min(Math.max(r.top + r.height / 2, 0), window.innerHeight - 1);
        var top = document.elementFromPoint(cx, cy);
        if (!top) return false;
        return top === el || el.contains(top) || top.contains(el);
    }
    function focusables() {
        var sel = 'a[href], button, input, select, textarea, [tabindex], [role="button"], video';
        return Array.prototype.filter.call(document.querySelectorAll(sel), function(el) {
            return isVisible(el) && !el.disabled && isTopMost(el);
        });
    }
    function rectCenter(r) { return { x: r.left + r.width / 2, y: r.top + r.height / 2 }; }

    function move(direction) {
        var candidates = focusables();
        var curEl = document.activeElement;
        var hasCurrent = curEl && curEl !== document.body && isVisible(curEl);
        if (!hasCurrent) {
            if (candidates[0]) {
                candidates[0].focus();
                candidates[0].scrollIntoView({ block: 'center', inline: 'center', behavior: 'smooth' });
            }
            return;
        }
        var curC = rectCenter(curEl.getBoundingClientRect());
        var best = null, bestScore = Infinity;
        candidates.forEach(function(el) {
            if (el === curEl) return;
            var r = el.getBoundingClientRect();
            var c = rectCenter(r);
            var dx = c.x - curC.x, dy = c.y - curC.y;
            var primary, ok;
            if (direction === 'down') { ok = dy > 4; primary = dy; }
            else if (direction === 'up') { ok = dy < -4; primary = -dy; }
            else if (direction === 'right') { ok = dx > 4; primary = dx; }
            else { ok = dx < -4; primary = -dx; }
            if (!ok) return;
            var perpendicular = (direction === 'up' || direction === 'down') ? Math.abs(dx) : Math.abs(dy);
            var score = primary + perpendicular * 2;
            if (score < bestScore) { bestScore = score; best = el; }
        });
        if (best) {
            best.focus();
            best.scrollIntoView({ block: 'center', inline: 'center', behavior: 'smooth' });
        }
    }
    function activate() {
        var el = document.activeElement;
        if (el && el !== document.body) el.click();
    }
    window.__tvNav = { move: move, activate: activate };
})();
"""

/**
 * Best-effort auto-fill for the site's own login form. The form is rendered
 * client-side (no static markup to inspect ahead of time), so this polls for
 * common email/password field selectors for a few seconds rather than
 * assuming a fixed layout. It only fills the fields - it never auto-submits,
 * so the user still confirms the login with the remote.
 */
private fun buildAutoFillScript(email: String, password: String): String {
    val emailLiteral = org.json.JSONObject.quote(email)
    val passwordLiteral = org.json.JSONObject.quote(password)
    return """
        (function() {
            var attempts = 0;
            var timer = setInterval(function() {
                attempts++;
                try {
                    var emailEl = document.querySelector('input[type="email"], input[name="email"], input[name="username"], input#email');
                    var passEl = document.querySelector('input[type="password"], input[name="password"], input#password');
                    if (emailEl && passEl) {
                        function setVal(el, value) {
                            var proto = Object.getPrototypeOf(el);
                            var setter = Object.getOwnPropertyDescriptor(proto, 'value') && Object.getOwnPropertyDescriptor(proto, 'value').set;
                            if (setter) { setter.call(el, value); } else { el.value = value; }
                            el.dispatchEvent(new Event('input', { bubbles: true }));
                            el.dispatchEvent(new Event('change', { bubbles: true }));
                        }
                        setVal(emailEl, $emailLiteral);
                        setVal(passEl, $passwordLiteral);
                        clearInterval(timer);
                    }
                } catch (e) {}
                if (attempts > 20) { clearInterval(timer); }
            }, 500);
        })();
    """.trimIndent()
}
