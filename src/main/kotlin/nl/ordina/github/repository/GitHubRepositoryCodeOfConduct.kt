package nl.ordina.github.repository

import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryCodeOfConduct(
    val key: String,
    val name: String,
    val url: String,
    val body: String,
    val html_url: String?
)