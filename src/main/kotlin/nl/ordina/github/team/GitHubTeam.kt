package nl.ordina.github.team

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.ordina.github.internal.GitHubTeamClient
import nl.ordina.github.repository.GitHubRepositoryPermissions

@Suppress("PropertyName")
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
    @Transient
    internal var teamClient: GitHubTeamClient? = null

    fun getMembers(): List<GitHubTeamMember> =
        requireClient().getMembers(organization, slug)

    fun addMember(username: String): Unit =
        requireClient().addMember(organization, slug, username)

    fun removeMember(username: String): Unit =
        requireClient().removeMember(organization, slug, username)

    fun getRepositories(): List<GitHubTeamRepository> =
        requireClient().getRepositories(organization, slug)

    fun addRepository(repositoryName: String, permission: String) =
        requireClient().addRepository(organization, slug, repositoryName, permission)

    private fun requireClient(): GitHubTeamClient =
        requireNotNull(teamClient) { "No HTTP client configured on GitHubTeam" }
}