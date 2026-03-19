@file:Suppress("PropertyName")

package nl.ordina.github.internal

import kotlinx.serialization.Serializable
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.organization.GitHubOrganizationInvite
import nl.ordina.github.organization.GitHubOrganizationMember
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.repository.GitHubRepositoryPermissions
import nl.ordina.github.team.GitHubTeam
import nl.ordina.github.team.GitHubTeamParent
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Status
import org.http4k.format.KotlinxSerialization.auto

internal class GitHubOrganizationClient(private val client: HttpHandler) {
    fun getOrganization(organizationName: String): GitHubOrganization? {
        val lens = Body.auto<GitHubOrganization>().toLens()
        val request = GetRequest("orgs/$organizationName")
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response).also { it.organizationClient = this }
            Status.NOT_FOUND -> null
            else -> null
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
            else -> null
        }
    }

    fun createTeam(
        organizationName: String,
        teamName: String,
        description: String? = null,
        privacy: String = "secret",
        parentTeamId: Int? = null
    ): GitHubTeam {
        val lens = Body.auto<GetTeamResponse>().toLens()
        val body = CreateTeamRequest(teamName, description, privacy, parentTeamId)
        val request = PostRequest("orgs/$organizationName/teams", body)

        return lens(client(request)).withOrganization(organizationName)
    }

    fun getRepositories(organizationName: String): List<GitHubRepository> {
        val repositoryClient = GitHubRepositoryClient(client)
        val request = PaginatedRequest<GetRepositoryResponse>("orgs/$organizationName/repos")

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
            else -> null
        }
    }

    @Serializable
    internal data class InviteRequest(
        val invitee_id: Int
    )

    @Serializable
    internal data class GetTeamResponse(
        val id: Int,
        val node_id: String,
        val name: String,
        val slug: String,
        val description: String? = null,
        val privacy: String? = null,
        val notification_setting: String? = null,
        val permission: String,
        val permissions: GitHubRepositoryPermissions? = null,
        val url: String,
        val html_url: String,
        val members_url: String,
        val repositories_url: String,
        val parent: GitHubTeamParent? = null
    ) {
        fun withOrganization(organizationName: String): GitHubTeam =
            GitHubTeam(
                organization = organizationName,
                id,
                node_id,
                name,
                slug,
                description,
                privacy,
                notification_setting,
                permission,
                permissions,
                url,
                html_url,
                members_url,
                repositories_url,
                parent
            )
    }

    @Serializable
    internal data class CreateTeamRequest(
        val name: String,
        val description: String? = null,
        val privacy: String = "secret",
        val parent_team_id: Int? = null
    )

    @Serializable
    internal data class GetRepositoryResponse(
        val id: Int,
        val name: String,
        val full_name: String
    ) {
        fun withOwner(owner: String, repositoryClient: GitHubRepositoryClient): GitHubRepository =
            GitHubRepository(
                owner = owner,
                id,
                name,
                full_name
            ).also { it.repositoryClient = repositoryClient }
    }
}