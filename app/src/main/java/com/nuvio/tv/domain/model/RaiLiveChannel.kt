package com.nuvio.tv.domain.model

/**
 * A RAI live TV channel. The slug matches raiplay.it/dirette/<slug>, which the
 * "official-raiplay-live" scraper uses to resolve the actual stream.
 */
data class RaiLiveChannel(
    val slug: String,
    val displayName: String,
    val colorHex: String,
    val logoUrl: String
)

object RaiLiveChannels {
    // Fetched live from raiplay.it/dirette/<slug>.json on 2026-09-04. Rai Radio 2
    // is a radio station, deliberately excluded from this TV channel list.
    val ALL: List<RaiLiveChannel> = listOf(
        RaiLiveChannel(
            slug = "rai1",
            displayName = "Rai 1",
            colorHex = "#4144C5",
            logoUrl = "https://www.raiplay.it/dl/img/2016/09/1473661951374Logo-Rai1.png"
        ),
        RaiLiveChannel(
            slug = "rai2",
            displayName = "Rai 2",
            colorHex = "#E61C23",
            logoUrl = "https://www.raiplay.it/dl/img/2016/09/1473662585214Logo-Rai2.png"
        ),
        RaiLiveChannel(
            slug = "rai3",
            displayName = "Rai 3",
            colorHex = "#138657",
            logoUrl = "https://www.raiplay.it/dl/img/2016/09/1473662801274Logo-Rai3.png"
        ),
        RaiLiveChannel(
            slug = "rai4",
            displayName = "Rai 4",
            colorHex = "#9D2FE5",
            logoUrl = "https://www.raiplay.it/dl/img/2016/09/1473662992107Logo-Rai4.png"
        ),
        RaiLiveChannel(
            slug = "rai5",
            displayName = "Rai 5",
            colorHex = "#606806",
            logoUrl = "https://www.raiplay.it/dl/img/2021/11/19/1637322377457_logo-rai5.png"
        ),
        RaiLiveChannel(
            slug = "raimovie",
            displayName = "Rai Movie",
            colorHex = "#7F0842",
            logoUrl = "https://www.raiplay.it/dl/img/2021/11/19/1637309933509_1579882457761_rai-movie.png"
        ),
        RaiLiveChannel(
            slug = "raipremium",
            displayName = "Rai Premium",
            colorHex = "#135F7E",
            logoUrl = "https://www.raiplay.it/dl/img/2021/11/19/1637309566388_1579882215002_rai-premium.png"
        ),
        RaiLiveChannel(
            slug = "rainews24",
            displayName = "Rai News 24",
            colorHex = "#0C32F1",
            logoUrl = "https://www.raiplay.it/dl/img/2016/07/1468515028111Rainews24-logo.png"
        ),
        RaiLiveChannel(
            slug = "raisport",
            displayName = "Rai Sport",
            colorHex = "#0C32F1",
            logoUrl = "https://www.raiplay.it/dl/img/2021/11/19/1637310367694_1579883330343_rai-sport.png"
        ),
        RaiLiveChannel(
            slug = "raistoria",
            displayName = "Rai Storia",
            colorHex = "#066855",
            logoUrl = "https://www.raiplay.it/dl/img/2021/11/19/1637309830740_1579882284354_rai-storia.png"
        ),
        RaiLiveChannel(
            slug = "raiscuola",
            displayName = "Rai Scuola",
            colorHex = "#BC5829",
            logoUrl = "https://www.raiplay.it/dl/img/2021/11/19/1637322361853_logo-raiscuola.png"
        ),
        RaiLiveChannel(
            slug = "raigulp",
            displayName = "Rai Gulp",
            colorHex = "#0C6EFD",
            logoUrl = "https://www.raiplay.it/dl/img/2021/11/19/1637310352874_1579882960255_rai-gulp.png"
        ),
        RaiLiveChannel(
            slug = "raiyoyo",
            displayName = "Rai YoYo",
            colorHex = "#02883A",
            logoUrl = "https://www.raiplay.it/dl/img/2021/11/19/1637309909658_1579882398754_rai-yoyo.png"
        )
    )
}
