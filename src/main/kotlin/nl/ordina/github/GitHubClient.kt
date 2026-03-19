package nl.ordina.github

import nl.ordina.github.internal.GitHubOrganizationClient
import nl.ordina.github.internal.GitHubRepositoryClient
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.repository.GitHubRepository
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters

class GitHubClient internal constructor(private val httpClient: HttpHandler) {
    private val organizationClient = GitHubOrganizationClient(httpClient)
    private val repositoryClient = GitHubRepositoryClient(httpClient)

    /**
     * Get an organization by its name
     */
    fun getOrganization(name: String): GitHubOrganization? =
        organizationClient.getOrganization(name)

    /**
     * Get a repository by its owner and name
     */
    fun getRepository(owner: String, repositoryName: String): GitHubRepository? =
        repositoryClient.getRepository(owner, repositoryName)

    /**
     * Get a list of repositories from an organization
     */
    fun getRepositories(organizationName: String): List<GitHubRepository> =
        organizationClient.getRepositories(organizationName)

    companion object {
        /**
         * Creates a client used for communication with the GitHub API
         *
         * @param token The GitHub token used for communication with the GitHub API
         * @param baseUrl The base url of the GitHub API, defaults to the Cloud GitHub API
         */
        fun create(token: String, baseUrl: String = "https://api.github.com"): GitHubClient {
            val httpClient: HttpHandler = ClientFilters
                .SetBaseUriFrom(Uri.of(baseUrl))
                .then(ClientFilters.BearerAuth.invoke(token))
                .then(JavaHttpClient())

            return GitHubClient(httpClient)
        }
    }
}