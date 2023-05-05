package nl.ordina.github.repository

import kotlinx.serialization.Serializable
import nl.ordina.github.internal.GitHubRepositoryClient
import nl.ordina.github.internal.GitHubRepositoryClient.Affiliation

@Serializable
data class GitHubRepository internal constructor(
    private val owner: String,
    val id: Int,
    val name: String,
    val full_name: String
) {

    fun getTeams() = GitHubRepositoryClient.getTeams(owner, name)
    fun getAllCollaborators() = GitHubRepositoryClient.getCollaborators(owner, name, Affiliation.ALL)
    fun getDirectCollaborators() = GitHubRepositoryClient.getCollaborators(owner, name, Affiliation.DIRECT)
    fun getOutsideCollaborators() = GitHubRepositoryClient.getCollaborators(owner, name, Affiliation.OUTSIDE)
    fun transfer(newOwner: String) = GitHubRepositoryClient.transfer(newOwner)
}