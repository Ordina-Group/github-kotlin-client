package nl.ordina.github.team

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubTeamParent(
    val id: Int,
    @SerialName("node_id") val nodeId: String,
    val url: String,
    @SerialName("members_url") val membersUrl: String,
    val name: String,
    val description: String? = null,
    val permission: String,
    val privacy: String? = null,
    @SerialName("notification_setting") val notificationSetting: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("repositories_url") val repositoriesUrl: String,
    val slug: String,
    @SerialName("ldap_dn") val ldapDn: String? = null
)