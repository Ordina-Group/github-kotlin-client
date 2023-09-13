package nl.ordina.github.internal

import kotlinx.serialization.Serializable
import nl.ordina.github.client
import nl.ordina.github.team.GitHubTeamMember
import nl.ordina.github.team.GitHubTeamRepository

internal object GitHubTeamClient {
    fun getMembers(organizationName: String, teamSlug: String): List<GitHubTeamMember> {
        val request = PaginatedRequest<GitHubTeamMember>("orgs/$organizationName/teams/$teamSlug/members")

        return request(client)
    }

    fun addMember(organizationName: String, teamSlug: String, username: String) {
        val request = PutRequest<Any>("/orgs/$organizationName/teams/$teamSlug/memberships/$username")

        client(request)
    }

    fun removeMember(organizationName: String, teamSlug: String, username: String) {
        val request = DeleteRequest<Any>("/orgs/$organizationName/teams/$teamSlug/memberships/$username")

        client(request)
    }

    fun getRepositories(organizationName: String, teamSlug: String): List<GitHubTeamRepository> {
        val request = PaginatedRequest<GitHubTeamRepository>("/orgs/$organizationName/teams/$teamSlug/repos")

        return request(client)
    }

    fun addRepository(organizationName: String, teamSlug: String, repositoryName: String, permission: String) {
        val uri = "/orgs/$organizationName/teams/$teamSlug/repos/$organizationName/$repositoryName"
        val request = PutRequest(uri, AddRepositoryRequest(permission))

        client(request)
    }

    @Serializable
    data class AddRepositoryRequest(val permission: String)
}