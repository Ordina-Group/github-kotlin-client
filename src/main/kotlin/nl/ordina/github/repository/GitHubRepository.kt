package nl.ordina.github.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.ordina.github.internal.GitHubRepositoryClient
import nl.ordina.github.internal.GitHubRepositoryClient.Affiliation

@Serializable
@ConsistentCopyVisibility
data class GitHubRepository internal constructor(
    private val owner: String,
    val id: Int,
    val name: String,
    @SerialName("full_name") val fullName: String
) {
    @Transient
    internal var repositoryClient: GitHubRepositoryClient? = null

    fun getTeams() = requireClient().getTeams(owner, name)
    fun getAllCollaborators() = requireClient().getCollaborators(owner, name, Affiliation.ALL)
    fun getDirectCollaborators() = requireClient().getCollaborators(owner, name, Affiliation.DIRECT)
    fun getOutsideCollaborators() = requireClient().getCollaborators(owner, name, Affiliation.OUTSIDE)
    fun transfer(newOwner: String) = requireClient().transfer(owner, name, newOwner)

    private fun requireClient(): GitHubRepositoryClient =
        requireNotNull(repositoryClient) { "No HTTP client configured on GitHubRepository" }
}