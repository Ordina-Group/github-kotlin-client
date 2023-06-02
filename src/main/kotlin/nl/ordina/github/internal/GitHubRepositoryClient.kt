package nl.ordina.github.internal

import kotlinx.serialization.Serializable
import nl.ordina.github.client
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.repository.GitHubRepositoryCollaborator
import nl.ordina.github.repository.GitHubRepositoryTeam
import org.http4k.core.Body
import org.http4k.core.Status
import org.http4k.format.KotlinxSerialization.auto

internal object GitHubRepositoryClient {

    fun getRepository(owner: String, repositoryName: String): GitHubRepository? {
        val lens = Body.auto<GetRepositoryResponse>().toLens()
        val request = GetRequest("repos/$owner/$repositoryName")
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response).withOwner(owner)
            Status.NOT_FOUND -> null
            // TODO Deal with unexpected responses
            else -> null
        }
    }

    fun getTeams(owner: String, repositoryName: String): List<GitHubRepositoryTeam> {
        val lens = Body.auto<List<GetTeamResponse>>().toLens()
        val request = GetRequest("/repos/$owner/$repositoryName/teams")
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response).map { it.withOrganization(owner) }
            else -> emptyList()
        }
    }

    fun getCollaborators(
        owner: String,
        repositoryName: String,
        affiliation: Affiliation
    ): List<GitHubRepositoryCollaborator> {
        val request = ListRequest<GitHubRepositoryCollaborator>("/repos/$owner/$repositoryName/collaborators") {
            it.query("affiliation", affiliation.value)
        }

        return request(client)
    }

    fun transfer(
        currentOwner: String,
        currentRepositoryName: String,
        newOwner: String,
        teamIds: List<Int> = emptyList(),
        newRepositoryName: String? = null
    ) {
        val request = PostRequest(
            "/repos/$currentOwner/$currentRepositoryName/transfer",
            TransferRepositoryRequest(newOwner, teamIds, newRepositoryName)
        )
        client(request)
    }

    @Serializable
    data class TransferRepositoryRequest(val new_owner: String, val team_ids: List<Int>, val new_name: String?)

    @Serializable
    data class GetRepositoryResponse(val id: Int, val name: String, val full_name: String) {
        fun withOwner(owner: String) = GitHubRepository(
            owner = owner,
            id,
            name,
            full_name
        )
    }

    @Serializable
    data class GetTeamResponse(
        val id: Int,
        val node_id: String,
        val url: String,
        val html_url: String,
        val name: String,
        val slug: String,
        val description: String,
        val privacy: String,
        val notification_setting: String,
        val permission: String,
        val members_url: String,
        val repositories_url: String
    ) {
        fun withOrganization(organization: String) = GitHubRepositoryTeam(
            organization = organization,
            id,
            node_id,
            url,
            html_url,
            name,
            slug,
            description,
            privacy,
            notification_setting,
            permission,
            members_url,
            repositories_url
        )
    }

    enum class Affiliation(val value: String) {
        ALL("all"),
        DIRECT("direct"),
        OUTSIDE("outside")
    }
}