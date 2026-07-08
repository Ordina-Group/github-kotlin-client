package com.soprasteria.github

import org.http4k.client.ApacheClient
import org.http4k.client.PreCannedApacheHttpClients
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference

class GitHubClient internal constructor(
    httpClient: HttpHandler,
    initialCloseAction: Closeable? = null,
) : Closeable {
    private val closeAction = AtomicReference<Closeable?>(initialCloseAction)

    val organizations = OrganizationService(httpClient)
    val repositories = RepositoryService(httpClient)
    val teams = TeamService(httpClient)

    override fun close() {
        closeAction.getAndSet(null)?.close()
    }

    companion object {
        /**
         * Creates a client used for communication with the GitHub API.
         *
         * @param token   The GitHub token used for authentication
         * @param baseUrl The base url of the GitHub API; defaults to the Cloud GitHub API
         */
        fun create(
            token: String,
            baseUrl: String = "https://api.github.com",
        ): GitHubClient {
            val apacheClient =
                PreCannedApacheHttpClients.defaultApacheHttpClient()
            return try {
                val httpClient: HttpHandler =
                    ClientFilters
                        .SetBaseUriFrom(Uri.of(baseUrl))
                        .then(ClientFilters.BearerAuth(token))
                        .then(ApacheClient(apacheClient))

                GitHubClient(httpClient, apacheClient)
            } catch (e: Exception) {
                apacheClient.close()
                throw e
            }
        }
    }
}