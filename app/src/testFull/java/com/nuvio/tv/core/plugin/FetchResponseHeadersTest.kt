package com.nuvio.tv.core.plugin

import okhttp3.Headers
import org.junit.Assert.assertEquals
import org.junit.Test

class FetchResponseHeadersTest {

    @Test
    fun `response headers preserve duplicate values`() {
        val headers = Headers.Builder()
            .add("X-Test", "Hello")
            .add("X-Test", "World")
            .build()

        assertEquals("Hello,World", headers.toPluginResponseHeaders()["x-test"])
    }

    // Regression test for a real login failure (HTTP 419 on streamingcommunity.vip):
    // a response setting both an XSRF-TOKEN and a session cookie comma-joins them
    // into a single "set-cookie" header value here, same as any other duplicate
    // header. That join is fine for most headers but is ambiguous for Set-Cookie
    // specifically, since cookie attributes like `expires=Wed, 21 Oct 2026...`
    // contain commas themselves - a consumer splitting on the first comma or
    // semicolon can silently truncate to just the first cookie. This is exactly
    // why the Fetch spec added Headers.getSetCookie() instead of relying on
    // Headers.get('set-cookie'); performNativeFetch works around it the same way,
    // by passing httpResponse.headers("Set-Cookie") through as a separate,
    // unjoined list (see PluginRuntime.performNativeFetch's setCookieList).
    @Test
    fun `joined set-cookie header is ambiguous and must not be parsed directly`() {
        val headers = Headers.Builder()
            .add("Set-Cookie", "XSRF-TOKEN=abc123; expires=Wed, 21 Oct 2026 07:28:00 GMT; path=/")
            .add("Set-Cookie", "streamingcommunity_session=xyz789; expires=Wed, 21 Oct 2026 07:28:00 GMT; httponly")
            .build()

        // The joined value both loses the split point between the two cookies
        // AND has extra commas from the `expires` attributes - unsplittable
        // back into two cookies by any simple separator.
        val joined = headers.toPluginResponseHeaders()["set-cookie"]
        assertEquals(
            "XSRF-TOKEN=abc123; expires=Wed, 21 Oct 2026 07:28:00 GMT; path=/," +
                "streamingcommunity_session=xyz789; expires=Wed, 21 Oct 2026 07:28:00 GMT; httponly",
            joined
        )

        // The fix: read Set-Cookie values individually instead of through the
        // joined map, exactly as performNativeFetch's setCookieList does.
        val individual = headers.values("Set-Cookie")
        assertEquals(2, individual.size)
        assertEquals("XSRF-TOKEN=abc123; expires=Wed, 21 Oct 2026 07:28:00 GMT; path=/", individual[0])
        assertEquals(
            "streamingcommunity_session=xyz789; expires=Wed, 21 Oct 2026 07:28:00 GMT; httponly",
            individual[1]
        )
    }
}
