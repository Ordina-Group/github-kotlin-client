package nl.ordina.github

import nl.ordina.github.internal.GitHubOrganizationClient
import nl.ordina.github.internal.GitHubRepositoryClient
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.repository.GitHubRepository
import org.http4k.core.HttpHandler

object Defaults {
    const val owner = "github"

    fun organization(httpClient: HttpHandler) = GitHubOrganization(
        login = owner,
        id = 1,
        node_id = "MDEyOk9yZ2FuaXphdGlvbjE=",
        name = "github",
        company = "GitHub"
    ).also { it.organizationClient = GitHubOrganizationClient(httpClient) }

    fun repository(httpClient: HttpHandler) = GitHubRepository(
        owner = owner,
        id = 1,
        name = "Mona-Liza",
        full_name = "github/Mona-Liza"
    ).also { it.repositoryClient = GitHubRepositoryClient(httpClient) }
}