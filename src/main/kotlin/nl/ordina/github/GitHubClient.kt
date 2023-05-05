package nl.ordina.github

import nl.ordina.github.internal.GitHubOrganizationClient
import nl.ordina.github.internal.GitHubRepositoryClient
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.repository.GitHubRepository
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters

lateinit var client: HttpHandler

class GitHubClient internal constructor(_client: HttpHandler) {
    init {
        client = _client
    }

    fun getOrganization(name: String): GitHubOrganization? =
        GitHubOrganizationClient.getOrganization(name)

    fun getRepository(owner: String, repositoryName: String): GitHubRepository? =
        GitHubRepositoryClient.getRepository(owner, repositoryName)

    fun getRepositories(organizationName: String): List<GitHubRepository> =
        GitHubOrganizationClient.getRepositories(organizationName)

    companion object {
        fun create(token: String, baseUrl: String = "https://api.github.com"): GitHubClient {
            val client: HttpHandler = ClientFilters
                .SetBaseUriFrom(Uri.of(baseUrl))
                .then(ClientFilters.BearerAuth.invoke(token))
                .then(JavaHttpClient())

            return GitHubClient(client)
        }
    }
}