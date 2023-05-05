package nl.ordina.github.repository

import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryPermissions internal constructor(
    val pull: Boolean? = null,
    val triage: Boolean? = null,
    val push: Boolean? = null,
    val maintain: Boolean? = null,
    val admin: Boolean? = null
)