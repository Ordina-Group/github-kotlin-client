package com.soprasteria.github.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.soprasteria.github.GitHubApiException
import com.soprasteria.github.organization.GitHubOrganization
import com.soprasteria.github.organization.GitHubOrganizationInvite
import com.soprasteria.github.organization.GitHubOrganizationMember
import com.soprasteria.github.repository.GitHubRepository
import com.soprasteria.github.repository.GitHubRepositoryPermissions
import com.soprasteria.github.team.GitHubTeam
import com.soprasteria.github.team.GitHubTeamParent
import com.soprasteria.github.team.TeamPrivacy
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Status
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory

internal class GitHubOrganizationClient(private val client: HttpHandler) {
    private val logger = LoggerFactory.getLogger(GitHubOrganizationClient::class.java)

    fun getOrganization(organizationName: String): GitHubOrganization? {
        logger.debug("Fetching organization '{}'", organizationName)
        val lens = Body.auto<GitHubOrganization>().toLens()
        val request = GetRequest(GitHubApiEndpoints.organization(organizationName))
        val response = client(request)

        return when (response.status) {
            Status.OK -> {
                logger.debug("Found organization '{}'", organizationName)
                lens(response)
            }
            Status.NOT_FOUND -> {
                logger.debug("Organization '{}' not found", organizationName)
                null
            }
            else -> throw GitHubApiException.from(response, "getOrganization($organizationName)")
        }
    }

    fun getTeams(organizationName: String): List<GitHubTeam> {
        val request = PaginatedRequest<GetTeamResponse>(GitHubApiEndpoints.organizationTeams(organizationName))
        return request(client).map { it.toTeam(organizationName) }
    }

    fun getTeam(organizationName: String, teamSlug: String): GitHubTeam? {
        val lens = getLens<GetTeamResponse>()
        val request = GetRequest(GitHubApiEndpoints.organizationTeam(organizationName, teamSlug))
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response).toTeam(organizationName)
            Status.NOT_FOUND -> null
            else -> throw GitHubApiException.from(response, "getTeam($organizationName, $teamSlug)")
        }
    }

    fun createTeam(
        organizationName: String,
        teamName: String,
        description: String? = null,
        privacy: TeamPrivacy = TeamPrivacy.Secret,
        parentTeamId: Int? = null
    ): GitHubTeam {
        val lens = Body.auto<GetTeamResponse>().toLens()
        val body = CreateTeamRequest(teamName, description, privacy.value, parentTeamId)
        val request = PostRequest(GitHubApiEndpoints.organizationTeams(organizationName), body)
        return lens(client(request)).toTeam(organizationName)
    }

    fun getRepositories(organizationName: String): List<GitHubRepository> {
        val request = PaginatedRequest<GitHubRepositoryClient.GetRepositoryResponse>(
            GitHubApiEndpoints.organizationRepositories(organizationName)
        )
        return request(client).map { it.toRepository(organizationName) }
    }

    fun getMembers(organizationName: String): List<GitHubOrganizationMember> {
        val request = PaginatedRequest<GitHubOrganizationMember>(GitHubApiEndpoints.organizationMembers(organizationName))
        return request(client)
    }

    fun invite(organizationName: String, inviteeId: Int): GitHubOrganizationInvite? {
        val lens = Body.auto<GitHubOrganizationInvite>().toLens()
        val request = PostRequest(GitHubApiEndpoints.organizationInvitations(organizationName), InviteRequest(inviteeId))
        val response = client(request)

        return when (response.status) {
            Status.CREATED -> lens(response)
            Status.NOT_FOUND -> null
            Status.UNPROCESSABLE_ENTITY -> null
            else -> throw GitHubApiException.from(response, "invite($organizationName, $inviteeId)")
        }
    }

    @Serializable
    internal data class InviteRequest(
        @SerialName("invitee_id") val inviteeId: Int
    )

    @Serializable
    internal data class GetTeamResponse(
        val id: Int,
        @SerialName("node_id") val nodeId: String,
        val name: String,
        val slug: String,
        val description: String? = null,
        val privacy: String? = null,
        @SerialName("notification_setting") val notificationSetting: String? = null,
        val permission: String,
        val permissions: GitHubRepositoryPermissions? = null,
        val url: String,
        @SerialName("html_url") val htmlUrl: String,
        @SerialName("members_url") val membersUrl: String,
        @SerialName("repositories_url") val repositoriesUrl: String,
        val parent: GitHubTeamParent? = null
    ) {
        fun toTeam(organizationName: String) = GitHubTeam(
            organization = organizationName,
            id = id,
            nodeId = nodeId,
            name = name,
            slug = slug,
            description = description,
            privacy = privacy,
            notificationSetting = notificationSetting,
            permission = permission,
            permissions = permissions,
            url = url,
            htmlUrl = htmlUrl,
            membersUrl = membersUrl,
            repositoriesUrl = repositoriesUrl,
            parent = parent
        )
    }

    @Serializable
    internal data class CreateTeamRequest(
        val name: String,
        val description: String? = null,
        val privacy: String = "secret",
        @SerialName("parent_team_id") val parentTeamId: Int? = null
    )
}