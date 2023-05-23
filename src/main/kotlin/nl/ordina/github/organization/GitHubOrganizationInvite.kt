package nl.ordina.github.organization

import kotlinx.serialization.Serializable
import nl.ordina.github.internal.GitHubOrganizationInviter

@Serializable
data class GitHubOrganizationInvite(
    val id: Int,
    val login: String?,
    val email: String?,
    val role: String,
    val created_at: String,
    val failed_at: String? = null,
    val failed_reason: String? = null,
    val inviter: GitHubOrganizationInviter,
    val team_count: Int,
    val node_id: String,
    val invitation_teams_url: String,
    val invitation_source: String? = null
)