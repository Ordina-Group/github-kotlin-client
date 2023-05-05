package nl.ordina.github.team

import kotlinx.serialization.Serializable

@Serializable
data class GitHubTeamParent(
    val id: Int,
    val node_id: String,
    val url: String,
    val members_url: String,
    val name: String,
    val description: String?,
    val permission: String,
    val privacy: String?,
    val notification_setting: String?,
    val html_url: String,
    val repositories_url: String,
    val slug: String,
    val ldap_dn: String?
)