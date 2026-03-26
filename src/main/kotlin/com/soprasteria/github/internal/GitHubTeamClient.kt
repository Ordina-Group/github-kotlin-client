package com.soprasteria.github.internal

import com.soprasteria.github.team.GitHubTeamMember
import com.soprasteria.github.team.GitHubTeamRepository
import kotlinx.serialization.Serializable
import org.http4k.core.HttpHandler
import org.slf4j.LoggerFactory

internal class GitHubTeamClient(
    private val client: HttpHandler,
) {
    private val logger = LoggerFactory.getLogger(GitHubTeamClient::class.java)

    fun getMembers(
        organizationName: String,
        teamSlug: String,
    ): List<GitHubTeamMember> {
        logger.debug("Fetching members of team '{}/{}' ", organizationName, teamSlug)
        val request =
            PaginatedRequest<GitHubTeamMember>(
                GitHubApiEndpoints.organizationTeamMembers(organizationName, teamSlug),
            )
        return request(client)
    }

    fun addMember(
        organizationName: String,
        teamSlug: String,
        username: String,
    ) {
        val request =
            PutRequest<Any>(GitHubApiEndpoints.organizationTeamMembership(organizationName, teamSlug, username))
        client(request)
    }

    fun removeMember(
        organizationName: String,
        teamSlug: String,
        username: String,
    ) {
        val request =
            DeleteRequest<Any>(GitHubApiEndpoints.organizationTeamMembership(organizationName, teamSlug, username))
        client(request)
    }

    fun getRepositories(
        organizationName: String,
        teamSlug: String,
    ): List<GitHubTeamRepository> {
        val request =
            PaginatedRequest<GitHubTeamRepository>(
                GitHubApiEndpoints.organizationTeamRepositories(organizationName, teamSlug),
            )
        return request(client)
    }

    /**
     * @param repoOwner the owner of the repository; for org repos this equals [organizationName],
     *                  but may differ for user repos or forks
     */
    fun addRepository(
        organizationName: String,
        teamSlug: String,
        repoOwner: String,
        repositoryName: String,
        permission: String,
    ) {
        val uri = GitHubApiEndpoints.organizationTeamRepository(organizationName, teamSlug, repoOwner, repositoryName)
        val request = PutRequest(uri, AddRepositoryRequest(permission))
        client(request)
    }

    @Serializable
    data class AddRepositoryRequest(
        val permission: String,
    )
}