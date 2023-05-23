package nl.ordina.github.internal

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Header

object GetRequest {
    operator fun invoke(uri: String): Request = Request(Method.GET, uri)
}

object ListRequest {
    inline operator fun <reified Output : Any> invoke(
        uri: String,
        noinline transform: (Request) -> Request
    ): (HttpHandler) -> List<Output> {
        val lens = getLens<List<Output>>()
        val request = GetRequest(uri).with(transform)

        return { handler: HttpHandler -> lens(handler(request)) }
    }
}

object PostRequest {
    inline operator fun <reified T : Any> invoke(uri: String, body: T): Request =
        Request(Method.POST, uri).with(getLens<T>() of body)
}

object PutRequest {
    inline operator fun <reified T : Any> invoke(uri: String, body: T? = null): Request {
        return if (body != null) {
            Request(Method.PUT, uri).with(getLens<T>() of body)
        } else {
            Request(Method.PUT, uri)
        }
    }
}

class PaginatedRequest<T : Any>(private val baseRequest: Request, private val lens: BiDiBodyLens<List<T>>) {
    private fun getPage(handler: HttpHandler, page: Int = 1): List<T> {
        val requestWithPage = baseRequest.query("page", page.toString())
        val response = handler(requestWithPage)

        return when (response.status) {
            Status.OK -> {
                val hasNext = Header.LINK(response).containsKey("next")

                if (hasNext) {
                    lens(response) + getPage(handler, page + 1)
                } else {
                    lens(response)
                }
            }

            else -> emptyList()
        }
    }

    operator fun invoke(handler: HttpHandler): List<T> = getPage(handler)

    companion object {
        inline operator fun <reified T : Any> invoke(uri: String) : PaginatedRequest<T> {
            val request = GetRequest(uri)
            val lens = getLens<List<T>>()

            return PaginatedRequest(request, lens)
        }
    }
}

inline fun <reified T : Any>getLens(): BiDiBodyLens<T> = Body.auto<T>().toLens()
