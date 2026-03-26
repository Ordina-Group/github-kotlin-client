package com.soprasteria.github

import com.soprasteria.github.internal.GitHubOrganizationClient
import com.soprasteria.github.internal.GitHubRepositoryClient
import com.soprasteria.github.organization.GitHubOrganization
import com.soprasteria.github.organization.GitHubOrganizationInvite
import com.soprasteria.github.organization.GitHubOrganizationMember
import com.soprasteria.github.repository.GitHubRepository
import com.soprasteria.github.team.GitHubTeam
import com.soprasteria.github.team.TeamPrivacy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.http4k.core.HttpHandler

/**
 * Operations on GitHub organizations.
 * Obtain via [GitHubClient.organizations].
 */
class OrganizationService internal constructor(
    httpClient: HttpHandler,
) {
    private val orgClient = GitHubOrganizationClient(httpClient)
    private val repoClient = GitHubRepositoryClient(httpClient)

    /** Fetches a single organization. Returns [ApiResult.NotFound] for HTTP 404. */
    suspend fun get(name: String): ApiResult<GitHubOrganization> =
        withContext(Dispatchers.IO) {
            apiResult { orgClient.getOrganization(name) }
        }

    /** Lists all teams in an organization. */
    suspend fun getTeams(organizationName: String): ApiResult<List<GitHubTeam>> =
        withContext(Dispatchers.IO) {
            apiListResult { orgClient.getTeams(organizationName) }
        }

    /** @see getTeams */
    suspend fun getTeams(org: GitHubOrganization): ApiResult<List<GitHubTeam>> = getTeams(org.login)

    /** Fetches a single team by slug. Returns [ApiResult.NotFound] for HTTP 404. */
    suspend fun getTeam(
        organizationName: String,
        teamSlug: String,
    ): ApiResult<GitHubTeam> =
        withContext(Dispatchers.IO) {
            apiResult { orgClient.getTeam(organizationName, teamSlug) }
        }

    /** @see getTeam */
    suspend fun getTeam(
        org: GitHubOrganization,
        teamSlug: String,
    ): ApiResult<GitHubTeam> = getTeam(org.login, teamSlug)

    /** Creates a new team in an organization. */
    suspend fun createTeam(
        organizationName: String,
        teamName: String,
        description: String? = null,
        privacy: TeamPrivacy = TeamPrivacy.Secret,
        parentTeamId: Int? = null,
    ): ApiResult<GitHubTeam> =
        withContext(Dispatchers.IO) {
            apiListResult {
                listOf(
                    orgClient.createTeam(organizationName, teamName, description, privacy, parentTeamId),
                )
            }.map { it.first() }
        }

    /** @see createTeam */
    suspend fun createTeam(
        org: GitHubOrganization,
        teamName: String,
        description: String? = null,
        privacy: TeamPrivacy = TeamPrivacy.Secret,
        parentTeamId: Int? = null,
    ): ApiResult<GitHubTeam> = createTeam(org.login, teamName, description, privacy, parentTeamId)

    /** Lists all repositories for an organization. */
    suspend fun getRepositories(organizationName: String): ApiResult<List<GitHubRepository>> =
        withContext(Dispatchers.IO) {
            apiListResult { orgClient.getRepositories(organizationName) }
        }

    /** @see getRepositories */
    suspend fun getRepositories(org: GitHubOrganization): ApiResult<List<GitHubRepository>> = getRepositories(org.login)

    /** Lists all members of an organization. */
    suspend fun getMembers(organizationName: String): ApiResult<List<GitHubOrganizationMember>> =
        withContext(Dispatchers.IO) {
            apiListResult { orgClient.getMembers(organizationName) }
        }

    /** @see getMembers */
    suspend fun getMembers(org: GitHubOrganization): ApiResult<List<GitHubOrganizationMember>> = getMembers(org.login)

    /** Invites a user (by GitHub user ID) to an organization. */
    suspend fun invite(
        organizationName: String,
        inviteeId: Int,
    ): ApiResult<GitHubOrganizationInvite> =
        withContext(Dispatchers.IO) {
            apiResult { orgClient.invite(organizationName, inviteeId) }
        }

    /** @see invite */
    suspend fun invite(
        org: GitHubOrganization,
        inviteeId: Int,
    ): ApiResult<GitHubOrganizationInvite> = invite(org.login, inviteeId)
}

/** Converts a nullable-returning block into an [ApiResult], catching [GitHubApiException]. */
internal inline fun <T : Any> apiResult(block: () -> T?): ApiResult<T> =
    try {
        block()?.let { ApiResult.Found(it) } ?: ApiResult.NotFound
    } catch (e: GitHubApiException) {
        ApiResult.Failure(e)
    }

/** Converts a list-returning block into an [ApiResult], catching [GitHubApiException]. */
internal inline fun <T : Any> apiListResult(block: () -> List<T>): ApiResult<List<T>> =
    try {
        ApiResult.Found(block())
    } catch (e: GitHubApiException) {
        ApiResult.Failure(e)
    }

/** Maps the [ApiResult.Found] value using [transform], passing through [ApiResult.NotFound] and [ApiResult.Failure]. */
internal fun <T : Any, R : Any> ApiResult<List<T>>.map(transform: (List<T>) -> R): ApiResult<R> =
    when (this) {
        is ApiResult.Found -> ApiResult.Found(transform(value))
        is ApiResult.NotFound -> ApiResult.NotFound
        is ApiResult.Failure -> ApiResult.Failure(exception)
    }