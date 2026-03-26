package com.soprasteria.github.organization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubOrganization(
    val login: String,
    val id: Int,
    @SerialName("node_id") val nodeId: String,
    val name: String? = null,
    val company: String? = null,
)