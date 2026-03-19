@file:Suppress("PropertyName")

package nl.ordina.github.organization

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.ordina.github.internal.GitHubOrganizationClient
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.team.GitHubTeam

@Serializable
@ConsistentCopyVisibility
data class GitHubOrganization internal constructor(
    val login: String,
    val id: Int,
    val node_id: String,
    val name: String? = null,
    val company: String? = null
) {
    @Transient
    internal var organizationClient: GitHubOrganizationClient? = null

    fun getTeams(): List<GitHubTeam> = requireClient().getTeams(login)

    fun getTeam(teamSlug: String): GitHubTeam? = requireClient().getTeam(login, teamSlug)

    fun createTeam(
        teamName: String,
        teamDescription: String? = null,
        privacy: String = "secret",
        parentTeamId: Int? = null
    ): GitHubTeam =
        requireClient().createTeam(login, teamName, teamDescription, privacy, parentTeamId)

    fun getRepositories(): List<GitHubRepository> = requireClient().getRepositories(login)
    fun getMembers(): List<GitHubOrganizationMember> = requireClient().getMembers(login)

    fun invite(inviteeId: Int): GitHubOrganizationInvite? = requireClient().invite(login, inviteeId)

    private fun requireClient(): GitHubOrganizationClient =
        requireNotNull(organizationClient) { "No HTTP client configured on GitHubOrganization" }
}