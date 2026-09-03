<div align="center">

  <img src="assets/brand/app_logo_wordmark.png" alt="Nuvio" width="300" />

  <p>
    A free, open-source media app for your phone, your desktop, and the TV you already own.
    <br />
    Bring your own sources. Nuvio turns them into a library with artwork, ratings, subtitles, and your place saved on every screen.
  </p>

  <p>This is <strong>brusus</strong>'s fork of <a href="https://github.com/NuvioMedia/NuvioTV">NuvioMedia/NuvioTV</a>, built for an Android TV box (Mi TV Stick 4K).</p>

  [GitHub releases](https://github.com/brusus/NuvioTV/releases/latest) · [Upstream project](https://github.com/NuvioMedia/NuvioTV) · [Upstream website](https://nuvio.tv)

</div>

## Get Nuvio TV (this fork)

- [Android TV APK](https://github.com/brusus/NuvioTV/releases/latest)

This fork uses its own package ID (`com.nuvio.tv.brusus`), so it installs side by side with the official app instead of conflicting with it, and updates itself in-app from this repo's own releases.

## What's different from upstream

- Per-provider login for JS scraper plugins that need a premium account on their source site (e.g. StreamingCommunity) - credentials are entered in-app, stored only on-device, and sent only to that provider's own site
- Fixed a bug where a response setting multiple `Set-Cookie` headers (e.g. a CSRF token plus a session cookie, both needed together for a premium login) silently lost everything but the first cookie
- Fixed the provider login dialog being unreachable with D-pad-only navigation (no touchscreen, as on a TV remote)

## Build from source

```bash
git clone https://github.com/brusus/NuvioTV.git
cd NuvioTV
./gradlew :app:assembleFullDebug
```

Nuvio TV is built with Kotlin, Jetpack Compose, TV Material 3, and Media3. Development requires Android Studio, a JDK, and the Android SDK.

## License

[GNU General Public License v3.0](./LICENSE)
