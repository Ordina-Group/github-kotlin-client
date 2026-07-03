package com.soprasteria.github.internal

import com.soprasteria.github.GitHubApiException
import com.soprasteria.github.repository.GitHubRepository
import com.soprasteria.github.repository.GitHubRepositoryCollaborator
import com.soprasteria.github.repository.GitHubRepositoryContributor
import com.soprasteria.github.repository.GitHubRepositoryTeam
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Header
import org.slf4j.LoggerFactory

internal class GitHubRepositoryClient(
    private val client: HttpHandler,
) {
    private val logger = LoggerFactory.getLogger(GitHubRepositoryClient::class.java)

    fun getRepository(
        owner: String,
        repositoryName: String,
    ): GitHubRepository? {
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

    fun getTeams(
        owner: String,
        repositoryName: String,
    ): List<GitHubRepositoryTeam> {
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
        affiliation: Affiliation,
    ): List<GitHubRepositoryCollaborator> {
        val request =
            ListRequest<GitHubRepositoryCollaborator>(
                GitHubApiEndpoints.repositoryCollaborators(owner, repositoryName),
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
        newRepositoryName: String? = null,
    ): Unit? {
        val request =
            PostRequest(
                GitHubApiEndpoints.repositoryTransfer(currentOwner, currentRepositoryName),
                TransferRepositoryRequest(newOwner, teamIds, newRepositoryName),
            )
        val response = client(request)
        return when (response.status) {
            Status.ACCEPTED -> Unit
            Status.NOT_FOUND -> null
            else -> throw GitHubApiException.from(response, "transfer($currentOwner/$currentRepositoryName -> $newOwner)")
        }
    }

    fun getContributors(
        owner: String,
        repositoryName: String,
        maxContributors: Int = 5,
    ): List<GitHubRepositoryContributor> {
        logger.debug("Fetching contributors for '{}/{}'", owner, repositoryName)
        val lens = Body.auto<List<GitHubRepositoryContributor>>().toLens()
        val request =
            GetRequest(GitHubApiEndpoints.repositoryContributors(owner, repositoryName))
                .query("per_page", maxContributors.toString())
        val response = client(request)

        return when (response.status) {
            Status.OK -> parseContributorsResponse(response, lens, owner, repositoryName)
            Status.NO_CONTENT -> {
                logger.debug("No contributors found for '{}/{}'", owner, repositoryName)
                emptyList()
            }
            Status.NOT_FOUND -> {
                logger.debug("Repository '{}/{}' not found or contributors disabled", owner, repositoryName)
                emptyList()
            }
            else -> throw GitHubApiException.from(response, "getContributors($owner, $repositoryName)")
        }
    }

    fun getAllContributors(
        owner: String,
        repositoryName: String,
    ): List<GitHubRepositoryContributor> {
        logger.debug("Fetching all contributors for '{}/{}'", owner, repositoryName)
        val lens = Body.auto<List<GitHubRepositoryContributor>>().toLens()
        val request =
            PaginatedRequest<GitHubRepositoryContributor>(
                GetRequest(GitHubApiEndpoints.repositoryContributors(owner, repositoryName)),
                lens,
                { response, page ->
                    when (response.status) {
                        Status.OK -> {
                            val contributors = parseContributorsResponse(response, lens, owner, repositoryName, page)
                            PaginatedPage(
                                items = contributors,
                                hasNext = contributors.isNotEmpty() && Header.LINK(response).containsKey("next"),
                            )
                        }
                        Status.NO_CONTENT,
                        Status.NOT_FOUND,
                        Status.FORBIDDEN,
                        -> PaginatedPage(emptyList(), hasNext = false)
                        else -> {
                            logger.warn(
                                "Unexpected status {} for contributors page {} of '{}/{}'",
                                response.status,
                                page,
                                owner,
                                repositoryName,
                            )
                            PaginatedPage(emptyList(), hasNext = false)
                        }
                    }
                },
            )
        return request(client)
    }

    private fun parseContributorsResponse(
        response: Response,
        lens: BiDiBodyLens<List<GitHubRepositoryContributor>>,
        owner: String,
        repositoryName: String,
        page: Int? = null,
    ): List<GitHubRepositoryContributor> =
        try {
            val bodyString = response.bodyString()
            if (bodyString.isBlank()) {
                if (page == null) {
                    logger.debug("Empty contributors list for '{}/{}'", owner, repositoryName)
                }
                emptyList()
            } else {
                lens(response)
            }
        } catch (e: kotlinx.serialization.SerializationException) {
            if (page == null) {
                logger.warn("Failed to parse contributors for '{}/{}': {}", owner, repositoryName, e.message)
            } else {
                logger.warn("Failed to parse contributors page {} for '{}/{}': {}", page, owner, repositoryName, e.message)
            }
            emptyList()
        }

    @Serializable
    data class TransferRepositoryRequest(
        @SerialName("new_owner") val newOwner: String,
        @SerialName("team_ids") val teamIds: List<Int>,
        @SerialName("new_name") val newName: String?,
    )

    @Serializable
    data class GetRepositoryResponse(
        val id: Int,
        val name: String,
        @SerialName("full_name") val fullName: String,
        val visibility: String? = null,
        @SerialName("pushed_at") val pushedAt: String? = null,
    ) {
        fun toRepository(owner: String) =
            GitHubRepository(
                owner = owner,
                id = id,
                name = name,
                fullName = fullName,
                visibility = visibility,
                pushedAt = pushedAt,
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
        @SerialName("repositories_url") val repositoriesUrl: String,
    ) {
        fun toRepositoryTeam(organization: String) =
            GitHubRepositoryTeam(
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
                repositoriesUrl = repositoriesUrl,
            )
    }

    enum class Affiliation(
        val value: String,
    ) {
        ALL("all"),
        DIRECT("direct"),
        OUTSIDE("outside"),
    }
}