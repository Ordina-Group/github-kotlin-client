package com.soprasteria.github.internal

import com.soprasteria.github.GitHubApiException
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Header
import org.slf4j.LoggerFactory

object GetRequest {
    operator fun invoke(uri: String): Request = Request(Method.GET, uri)
}

object ListRequest {
    inline operator fun <reified Output : Any> invoke(
        uri: String,
        noinline transform: (Request) -> Request,
    ): (HttpHandler) -> List<Output> {
        val lens = getLens<List<Output>>()
        val request = GetRequest(uri).with(transform)

        return { handler: HttpHandler -> lens(handler(request)) }
    }
}

object PostRequest {
    inline operator fun <reified T : Any> invoke(
        uri: String,
        body: T,
    ): Request = Request(Method.POST, uri).with(getLens<T>() of body)
}

object PutRequest {
    inline operator fun <reified T : Any> invoke(
        uri: String,
        body: T? = null,
    ): Request =
        if (body != null) {
            Request(Method.PUT, uri).with(getLens<T>() of body)
        } else {
            Request(Method.PUT, uri)
        }
}

object PatchRequest {
    inline operator fun <reified T : Any> invoke(
        uri: String,
        body: T,
    ): Request = Request(Method.PATCH, uri).with(getLens<T>() of body)
}

object DeleteRequest {
    inline operator fun <reified T : Any> invoke(
        uri: String,
        body: T? = null,
    ): Request =
        if (body != null) {
            Request(Method.DELETE, uri).with(getLens<T>() of body)
        } else {
            Request(Method.DELETE, uri)
        }
}

private const val PAGE_SIZE = 100
private val logger = LoggerFactory.getLogger(PaginatedRequest::class.java)

data class PaginatedPage<T : Any>(
    val items: List<T>,
    val hasNext: Boolean,
)

class PaginatedRequest<T : Any>(
    private val baseRequest: Request,
    private val lens: BiDiBodyLens<List<T>>? = null,
    pageResult: ((Response, Int) -> PaginatedPage<T>)? = null,
) {
    init {
        require(pageResult != null || lens != null) {
            "A lens is required when no custom pageResult is provided"
        }
    }

    private val getPageResult = pageResult ?: { response: Response, _: Int -> defaultPageResult(response) }

    private fun defaultPageResult(response: Response): PaginatedPage<T> =
        when (response.status) {
            Status.OK -> PaginatedPage(requireNotNull(lens)(response), Header.LINK(response).containsKey("next"))
            else -> throw GitHubApiException.from(response, baseRequest.uri.toString())
        }

    private fun getPage(
        handler: HttpHandler,
        page: Int = 1,
    ): List<T> {
        val requestWithPage =
            baseRequest
                .query("page", page.toString())
                .query("per_page", PAGE_SIZE.toString())

        logger.debug("Fetching page {} of {}", page, baseRequest.uri)
        val response = handler(requestWithPage)
        val pageResult = getPageResult(response, page)

        logger.debug(
            "Page {} of {} returned {} items, hasNext={}",
            page,
            baseRequest.uri,
            pageResult.items.size,
            pageResult.hasNext,
        )

        return if (pageResult.hasNext) {
            pageResult.items + getPage(handler, page + 1)
        } else {
            pageResult.items
        }
    }

    operator fun invoke(handler: HttpHandler): List<T> = getPage(handler)

    companion object {
        inline operator fun <reified T : Any> invoke(uri: String): PaginatedRequest<T> {
            val request = GetRequest(uri)
            val lens = getLens<List<T>>()

            return PaginatedRequest(request, lens)
        }
    }
}

inline fun <reified T : Any> getLens(): BiDiBodyLens<T> = Body.auto<T>().toLens()