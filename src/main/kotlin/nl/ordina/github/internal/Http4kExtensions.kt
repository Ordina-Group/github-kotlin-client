package nl.ordina.github.internal

import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

object GetRequest {
    operator fun invoke(uri: String): Request = Request(Method.GET, uri)
}

object PostRequest {
    inline operator fun <reified T : Any> invoke(uri: String, body: T): Request =
        Request(Method.POST, uri).with(getLens<T>() of body)
}

inline fun <reified T : Any>getLens(): BiDiBodyLens<T> = Body.auto<T>().toLens()