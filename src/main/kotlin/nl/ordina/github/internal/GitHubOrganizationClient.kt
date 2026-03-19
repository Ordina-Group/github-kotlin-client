@file:Suppress("PropertyName")

package nl.ordina.github.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.ordina.github.GitHubApiException
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.organization.GitHubOrganizationInvite
import nl.ordina.github.organization.GitHubOrganizationMember
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.repository.GitHubRepositoryPermissions
import nl.ordina.github.team.GitHubTeam
import nl.ordina.github.team.GitHubTeamParent
import nl.ordina.github.team.TeamPrivacy
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
        val request = GetRequest("orgs/$organizationName")
        val response = client(request)

        return when (response.status) {
            Status.OK -> {
                logger.debug("Found organization '{}'", organizationName)
                lens(response).also { it.organizationClient = this }
            }
            Status.NOT_FOUND -> {
                logger.debug("Organization '{}' not found", organizationName)
                null
            }
            else -> throw GitHubApiException.from(response, "getOrganization($organizationName)")
        }
    }

    fun getTeams(organizationName: String): List<GitHubTeam> {
        val request = PaginatedRequest<GetTeamResponse>("orgs/$organizationName/teams")

        return request(client).map { it.withOrganization(organizationName) }
    }

    fun getTeam(organizationName: String, teamSlug: String): GitHubTeam? {
        val lens = getLens<GetTeamResponse>()
        val request = GetRequest("/orgs/$organizationName/teams/$teamSlug")
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response).withOrganization(organizationName)
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
        val request = PostRequest("orgs/$organizationName/teams", body)

        return lens(client(request)).withOrganization(organizationName)
    }

    fun getRepositories(organizationName: String): List<GitHubRepository> {
        val repositoryClient = GitHubRepositoryClient(client)
        val request = PaginatedRequest<GitHubRepositoryClient.GetRepositoryResponse>("orgs/$organizationName/repos")

        return request(client).map { it.withOwner(organizationName, repositoryClient) }
    }

    fun getMembers(organizationName: String): List<GitHubOrganizationMember> {
        val request = PaginatedRequest<GitHubOrganizationMember>("orgs/$organizationName/members")

        return request(client)
    }

    fun invite(organizationName: String, inviteeId: Int): GitHubOrganizationInvite? {
        val lens = Body.auto<GitHubOrganizationInvite>().toLens()
        val request = PostRequest("/orgs/$organizationName/invitations", InviteRequest(inviteeId))
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
        fun withOrganization(organizationName: String): GitHubTeam =
            GitHubTeam(
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