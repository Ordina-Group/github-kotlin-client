package nl.ordina.github.organization

import kotlinx.serialization.Serializable
import nl.ordina.github.internal.GitHubOrganizationClient
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.team.GitHubTeam

@Serializable
data class GitHubOrganization internal constructor(
    val login: String,
    val id: Int,
    val node_id: String,
    val name: String,
    val company: String? = null
) {
    fun getTeams(): List<GitHubTeam> = GitHubOrganizationClient.getTeams(login)

    fun createTeam(
        teamName: String,
        teamDescription: String? = null,
        privacy: String = "secret",
        parentTeamId: Int? = null
    ): GitHubTeam =
        GitHubOrganizationClient.createTeam(login, teamName, teamDescription, privacy, parentTeamId)

    fun getRepositories(): List<GitHubRepository> = GitHubOrganizationClient.getRepositories(login)
    fun getMembers(): List<GitHubOrganizationMember> = GitHubOrganizationClient.getMembers(login)

    fun invite(inviteeId: Int): GitHubOrganizationInvite? = GitHubOrganizationClient.invite(login, inviteeId)
}