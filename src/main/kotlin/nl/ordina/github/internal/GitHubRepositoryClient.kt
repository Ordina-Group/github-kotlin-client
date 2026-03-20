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
        val request = GetRequest(GitHubApiEndpoints.repository(owner, repositoryName))
        val response = client(request)

        return when (response.status) {
            Status.OK -> {
                logger.debug("Found repository '{}/{}'", owner, repositoryName)
                lens(response).toRepository(owner)
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
        val request = GetRequest(GitHubApiEndpoints.repositoryTeams(owner, repositoryName))
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response).map { it.toRepositoryTeam(owner) }
            else -> throw GitHubApiException.from(response, "getTeams($owner, $repositoryName)")
        }
    }

    fun getCollaborators(
        owner: String,
        repositoryName: String,
        affiliation: Affiliation
    ): List<GitHubRepositoryCollaborator> {
        val request = ListRequest<GitHubRepositoryCollaborator>(
            GitHubApiEndpoints.repositoryCollaborators(owner, repositoryName)
        ) {
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
            GitHubApiEndpoints.repositoryTransfer(currentOwner, currentRepositoryName),
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
    data class GetRepositoryResponse(
        val id: Int,
        val name: String,
        @SerialName("full_name") val fullName: String
    ) {
        fun toRepository(owner: String) = GitHubRepository(
            owner = owner,
            id = id,
            name = name,
            fullName = fullName
        )
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
        fun toRepositoryTeam(organization: String) = GitHubRepositoryTeam(
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
        )
    }

    enum class Affiliation(val value: String) {
        ALL("all"),
        DIRECT("direct"),
        OUTSIDE("outside")
    }
}