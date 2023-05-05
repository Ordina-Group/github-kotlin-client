package nl.ordina.github.team

import kotlinx.serialization.Serializable
import nl.ordina.github.internal.GitHubTeamClient
import nl.ordina.github.repository.GitHubRepositoryPermissions

@Serializable
data class GitHubTeam(
    private val organization: String,
    val id: Int,
    val node_id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val privacy: String?,
    val notification_setting: String?,
    val permission: String,
    val permissions: GitHubRepositoryPermissions?,
    val url: String,
    val html_url: String,
    val members_url: String,
    val repositories_url: String,
    val parent: GitHubTeamParent?
) {
    fun getMembers(): List<GitHubTeamMember> = GitHubTeamClient.getMembers(organization, slug)
    fun getRepositories(): List<GitHubTeamRepository> = GitHubTeamClient.getRepositories(organization, slug)
}