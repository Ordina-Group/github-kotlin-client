package com.soprasteria.github.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryTeam(
    val organization: String,
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
    val parent: GitHubRepositoryTeam? = null,
)