package com.soprasteria.github

import org.http4k.client.ApacheClient
import org.http4k.client.PreCannedApacheHttpClients
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import java.io.Closeable

class GitHubClient internal constructor(
    httpClient: HttpHandler,
    private var closeAction: Closeable? = null,
) : Closeable {
    val organizations = OrganizationService(httpClient)
    val repositories = RepositoryService(httpClient)
    val teams = TeamService(httpClient)

    override fun close() {
        val ownedResource = closeAction ?: return
        closeAction = null
        ownedResource.close()
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
            val httpClient: HttpHandler =
                ClientFilters
                    .SetBaseUriFrom(Uri.of(baseUrl))
                    .then(ClientFilters.BearerAuth(token))
                    .then(ApacheClient(apacheClient))

            return GitHubClient(httpClient, apacheClient)
        }
    }
}