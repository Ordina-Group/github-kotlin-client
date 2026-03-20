package com.soprasteria.github.team

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.soprasteria.github.repository.GitHubRepositoryPermissions

@Serializable
data class GitHubTeam(
    val organization: String,
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
)