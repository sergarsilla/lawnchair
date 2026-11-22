package app.lawnchair.search.algorithms.data

import android.net.Uri
import androidx.core.net.toUri

/** Recognises a web address typed into search, such as "example.org" or "nas.local:8080/path". */
object TypedAddress {

    data class Match(val uri: Uri, val displayText: String)

    private val withScheme = Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)

    // A dotted host whose last label starts with a letter (so "3.14" is not an address),
    // or an IPv4 address, followed by an optional port and path.
    private val bareAddress = Regex(
        "^((?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z][a-z0-9-]*[a-z0-9]|\\d{1,3}(?:\\.\\d{1,3}){3})(:\\d{1,5})?(/\\S*)?$",
        RegexOption.IGNORE_CASE,
    )

    // Hosts that usually live on a private network and are served over plain HTTP.
    private val localSuffixes = listOf(".internal", ".local", ".lan", ".home", ".home.arpa", ".localhost")
    private val ipv4 = Regex("^\\d{1,3}(?:\\.\\d{1,3}){3}$")

    fun parse(query: String): Match? {
        val text = query.trim()
        if (text.isEmpty() || text.any { it.isWhitespace() }) return null
        if (withScheme.matches(text)) return Match(text.toUri(), text)

        val match = bareAddress.matchEntire(text) ?: return null
        val host = match.groupValues[1].lowercase()
        val isLocal = ipv4.matches(host) || localSuffixes.any { host.endsWith(it) }
        val scheme = if (isLocal) "http" else "https"
        return Match("$scheme://$text".toUri(), text)
    }
}
