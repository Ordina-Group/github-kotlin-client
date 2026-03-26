package com.soprasteria.github

import com.soprasteria.github.internal.GitHubTeamClient
import com.soprasteria.github.repository.Permission
import com.soprasteria.github.team.GitHubTeam
import com.soprasteria.github.team.GitHubTeamMember
import com.soprasteria.github.team.GitHubTeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.http4k.core.HttpHandler

/**
 * Operations on GitHub teams.
 * Obtain via [GitHubClient.teams].
 */
class TeamService internal constructor(
    httpClient: HttpHandler,
) {
    private val teamClient = GitHubTeamClient(httpClient)

    /** Lists all members of a team. */
    suspend fun getMembers(
        organizationName: String,
        teamSlug: String,
    ): ApiResult<List<GitHubTeamMember>> =
        withContext(Dispatchers.IO) {
            apiListResult { teamClient.getMembers(organizationName, teamSlug) }
        }

    /** @see getMembers */
    suspend fun getMembers(team: GitHubTeam): ApiResult<List<GitHubTeamMember>> = getMembers(team.organization, team.slug)

    /** Adds a member to a team. */
    suspend fun addMember(
        organizationName: String,
        teamSlug: String,
        username: String,
    ) = withContext(Dispatchers.IO) {
        teamClient.addMember(organizationName, teamSlug, username)
    }

    /** @see addMember */
    suspend fun addMember(
        team: GitHubTeam,
        username: String,
    ) = addMember(team.organization, team.slug, username)

    /** Removes a member from a team. */
    suspend fun removeMember(
        organizationName: String,
        teamSlug: String,
        username: String,
    ) = withContext(Dispatchers.IO) {
        teamClient.removeMember(organizationName, teamSlug, username)
    }

    /** @see removeMember */
    suspend fun removeMember(
        team: GitHubTeam,
        username: String,
    ) = removeMember(team.organization, team.slug, username)

    /** Lists all repositories accessible to a team. */
    suspend fun getRepositories(
        organizationName: String,
        teamSlug: String,
    ): ApiResult<List<GitHubTeamRepository>> =
        withContext(Dispatchers.IO) {
            apiListResult { teamClient.getRepositories(organizationName, teamSlug) }
        }

    /** @see getRepositories */
    suspend fun getRepositories(team: GitHubTeam): ApiResult<List<GitHubTeamRepository>> = getRepositories(team.organization, team.slug)

    /**
     * Grants a team access to a repository.
     *
     * @param organizationName the organization that owns the team
     * @param teamSlug         the team slug
     * @param repositoryName   the repository name
     * @param permission       the permission to grant
     * @param repoOwner        the owner of the repository; defaults to [organizationName] for org repos
     */
    suspend fun addRepository(
        organizationName: String,
        teamSlug: String,
        repositoryName: String,
        permission: Permission,
        repoOwner: String = organizationName,
    ) = withContext(Dispatchers.IO) {
        teamClient.addRepository(organizationName, teamSlug, repoOwner, repositoryName, permission.value)
    }

    /** @see addRepository */
    suspend fun addRepository(
        team: GitHubTeam,
        repositoryName: String,
        permission: Permission,
    ) = addRepository(team.organization, team.slug, repositoryName, permission)
}