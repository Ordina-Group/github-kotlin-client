package nl.ordina.github.repository

import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryLicense(
    val key: String,
    val name: String,
    val spdx_id: String,
    val url: String,
    val node_id: String
)