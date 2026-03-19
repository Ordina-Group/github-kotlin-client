@file:Suppress("PropertyName")

package nl.ordina.github.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.ordina.github.GitHubApiException
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.repository.GitHubRepositoryCollaborator
import nl.ordina.github.repository.GitHubRepositoryTeam
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Status
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory

internal class GitHubRepositoryClient(private val client: HttpHandler) {
    private val logger = LoggerFactory.getLogger(GitHubRepositoryClient::class.java)

    fun getRepository(owner: String, repositoryName: String): GitHubRepository? {
        logger.debug("Fetching repository '{}/{}'", owner, repositoryName)
        val lens = Body.auto<GetRepositoryResponse>().toLens()
        val request = GetRequest("repos/$owner/$repositoryName")
        val response = client(request)

        return when (response.status) {
            Status.OK -> {
                logger.debug("Found repository '{}/{}'", owner, repositoryName)
                lens(response).withOwner(owner, this)
            }
            Status.NOT_FOUND -> {
                logger.debug("Repository '{}/{}' not found", owner, repositoryName)
                null
            }
            else -> throw GitHubApiException.from(response, "getRepository($owner, $repositoryName)")
        }
    }

    fun getTeams(owner: String, repositoryName: String): List<GitHubRepositoryTeam> {
        val lens = Body.auto<List<GetTeamResponse>>().toLens()
        val request = GetRequest("/repos/$owner/$repositoryName/teams")
        val response = client(request)
        val teamClient = GitHubTeamClient(client)

        return when (response.status) {
            Status.OK -> lens(response).map { it.withOrganization(owner, teamClient) }
            else -> throw GitHubApiException.from(response, "getTeams($owner, $repositoryName)")
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
    data class TransferRepositoryRequest(
        @SerialName("new_owner") val newOwner: String,
        @SerialName("team_ids") val teamIds: List<Int>,
        @SerialName("new_name") val newName: String?
    )

    @Serializable
    data class GetRepositoryResponse(val id: Int, val name: String, @SerialName("full_name") val fullName: String) {
        fun withOwner(owner: String, repositoryClient: GitHubRepositoryClient) = GitHubRepository(
            owner = owner,
            id = id,
            name = name,
            fullName = fullName
        ).also { it.repositoryClient = repositoryClient }
    }

    @Serializable
    data class GetTeamResponse(
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
        @SerialName("repositories_url") val repositoriesUrl: String
    ) {
        fun withOrganization(organization: String, teamClient: GitHubTeamClient) = GitHubRepositoryTeam(
            organization = organization,
            id = id,
            nodeId = nodeId,
            url = url,
            htmlUrl = htmlUrl,
            name = name,
            slug = slug,
            description = description,
            privacy = privacy,
            notificationSetting = notificationSetting,
            permission = permission,
            membersUrl = membersUrl,
            repositoriesUrl = repositoriesUrl
        ).also { it.teamClient = teamClient }
    }

    enum class Affiliation(val value: String) {
        ALL("all"),
        DIRECT("direct"),
        OUTSIDE("outside")
    }
}