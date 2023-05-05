package nl.ordina.github.repository

import kotlinx.serialization.Serializable
import nl.ordina.github.internal.GitHubTeamClient
import nl.ordina.github.team.GitHubTeamMember
import nl.ordina.github.team.GitHubTeamRepository

@Serializable
data class GitHubRepositoryTeam(
    private val organization: String,
    val id: Int,
    val node_id: String,
    val url: String,
    val html_url: String,
    val name: String,
    val slug: String,
    val description: String,
    val privacy: String,
    val notification_setting: String,
    val permission: String,
    val members_url: String,
    val repositories_url: String,
    val parent: GitHubRepositoryTeam? = null
) {
    fun getMembers(): List<GitHubTeamMember> = GitHubTeamClient.getMembers(organization, slug)
    fun getRepositories(): List<GitHubTeamRepository> = GitHubTeamClient.getRepositories(
        organization,
        slug
    )
}