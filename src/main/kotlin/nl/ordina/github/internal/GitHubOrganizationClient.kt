package nl.ordina.github.internal

import kotlinx.serialization.Serializable
import nl.ordina.github.*
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.organization.GitHubOrganizationInvite
import nl.ordina.github.organization.GitHubOrganizationMember
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.repository.GitHubRepositoryPermissions
import nl.ordina.github.team.GitHubTeam
import nl.ordina.github.team.GitHubTeamParent
import org.http4k.core.Body
import org.http4k.core.Status
import org.http4k.format.KotlinxSerialization.auto

internal object GitHubOrganizationClient {
    fun getOrganization(organizationName: String): GitHubOrganization? {
        val lens = Body.auto<GitHubOrganization>().toLens()
        val request = GetRequest("orgs/$organizationName")
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response)
            Status.NOT_FOUND -> null
            else -> null
        }
    }

    fun getTeams(organizationName: String): List<GitHubTeam> {
        val lens = Body.auto<List<GetTeamResponse>>().toLens()
        val request = GetRequest("orgs/$organizationName/teams")

        return client(request).let(lens).map { it.withOrganization(organizationName) }
    }

    fun getRepositories(organizationName: String): List<GitHubRepository> {
        val lens = Body.auto<List<GetRepositoryResponse>>().toLens()
        val request = GetRequest("orgs/$organizationName/repos")
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response).map { it.withOwner(organizationName) }
            Status.NOT_FOUND -> emptyList()
            // TODO Deal with unexpected responses
            else -> emptyList()
        }
    }

    fun getMembers(organizationName: String): List<GitHubOrganizationMember> {
        val lens = Body.auto<List<GitHubOrganizationMember>>().toLens()
        val request = GetRequest("orgs/$organizationName/members")
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response)
            Status.NOT_FOUND -> emptyList()
            // TODO Deal with unexpected responses
            else -> emptyList()
        }
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
    internal data class GetRepositoryResponse(
        val id: Int,
        val name: String,
        val full_name: String
    ) {
        fun withOwner(owner: String): GitHubRepository =
            GitHubRepository(
                owner = owner,
                id,
                name,
                full_name
            )
    }
}

data class GitHubOrganizationInviter(
    val name: String? = null,
    val email: String? = null,
    val login: String,
    val id: Int,
    val node_id: String,
    val avatar_url: String,
    val gravatar_id: String? = null,
    val url: String,
    val html_url: String,
    val followers_url: String,
    val following_url: String,
    val gists_url: String,
    val starred_url: String,
    val subscriptions_url: String,
    val organizations_url: String,
    val repos_url: String,
    val events_url: String,
    val received_events_url: String,
    val type: String,
    val site_admin: Boolean,
    val starred_at: String? = null
)