package nl.ordina.github.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.ordina.github.internal.GitHubTeamClient
import nl.ordina.github.team.GitHubTeamMember
import nl.ordina.github.team.GitHubTeamRepository

@Serializable
data class GitHubRepositoryTeam(
    private val organization: String,
    val id: Int,
    @SerialName("node_id") val nodeId: String,
    val url: String,
    @SerialName("html_url") val htmlUrl: String,
    val name: String,
    val slug: String,
    val description: String,
    val privacy: String,
    @SerialName("notification_setting") val notificationSetting: String,
    val permission: String,
    @SerialName("members_url") val membersUrl: String,
    @SerialName("repositories_url") val repositoriesUrl: String,
    val parent: GitHubRepositoryTeam? = null
) {
    @Transient
    internal var teamClient: GitHubTeamClient? = null

    fun getMembers(): List<GitHubTeamMember> = requireClient().getMembers(organization, slug)
    fun getRepositories(): List<GitHubTeamRepository> = requireClient().getRepositories(organization, slug)

    private fun requireClient(): GitHubTeamClient =
        requireNotNull(teamClient) { "No HTTP client configured on GitHubRepositoryTeam" }
}