package com.soprasteria.github.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryLicense(
    val key: String,
    val name: String,
    @SerialName("spdx_id") val spdxId: String,
    val url: String? = null,
    @SerialName("node_id") val nodeId: String,
)