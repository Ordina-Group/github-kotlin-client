package nl.ordina.github

import io.mockk.Matcher
import io.mockk.MockKMatcherScope
import org.http4k.core.Request

fun MockKMatcherScope.matchUri(uri: String): Request = match(UriMatcher(uri))

class UriMatcher(private val uri: String) : Matcher<Request> {
    override fun match(arg: Request?): Boolean = arg != null && arg.uri.toString() == uri
}