package nl.ordina.github.team

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.ordina.github.internal.GitHubTeamClient
import nl.ordina.github.repository.GitHubRepositoryPermissions

@Serializable
data class GitHubTeam(
    private val organization: String,
    val id: Int,
    @SerialName("node_id") val nodeId: String,
    val name: String,
    val slug: String,
    val description: String?,
    val privacy: String?,
    @SerialName("notification_setting") val notificationSetting: String?,
    val permission: String,
    val permissions: GitHubRepositoryPermissions?,
    val url: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("members_url") val membersUrl: String,
    @SerialName("repositories_url") val repositoriesUrl: String,
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