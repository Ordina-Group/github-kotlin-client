package com.soprasteria.github.organization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubOrganizationInvite(
    val id: Int,
    val login: String?,
    val email: String?,
    val role: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("failed_at") val failedAt: String? = null,
    @SerialName("failed_reason") val failedReason: String? = null,
    val inviter: GitHubOrganizationInviter,
    @SerialName("team_count") val teamCount: Int,
    @SerialName("node_id") val nodeId: String,
    @SerialName("invitation_teams_url") val invitationTeamsUrl: String,
    @SerialName("invitation_source") val invitationSource: String? = null
)