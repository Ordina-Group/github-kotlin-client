package nl.ordina.github

import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters

class GitHubClient internal constructor(httpClient: HttpHandler) {
    val organizations = OrganizationService(httpClient)
    val repositories = RepositoryService(httpClient)
    val teams = TeamService(httpClient)

    companion object {
        /**
         * Creates a client used for communication with the GitHub API.
         *
         * @param token   The GitHub token used for authentication
         * @param baseUrl The base url of the GitHub API; defaults to the Cloud GitHub API
         */
        fun create(token: String, baseUrl: String = "https://api.github.com"): GitHubClient {
            val httpClient: HttpHandler = ClientFilters
                .SetBaseUriFrom(Uri.of(baseUrl))
                .then(ClientFilters.BearerAuth(token))
                .then(ApacheClient())

            return GitHubClient(httpClient)
        }
    }
}