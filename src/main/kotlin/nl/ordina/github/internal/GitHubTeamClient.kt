package nl.ordina.github.internal

import nl.ordina.github.*
import nl.ordina.github.team.GitHubTeamMember
import nl.ordina.github.team.GitHubTeamRepository
import org.http4k.core.Status

internal object GitHubTeamClient {

    fun getMembers(organizationName: String, teamSlug: String): List<GitHubTeamMember> {
        val lens = getLens<List<GitHubTeamMember>>()
        val request = GetRequest("orgs/$organizationName/teams/$teamSlug/members")

        return client(request).let(lens)
    }

    fun getRepositories(organizationName: String, teamSlug: String): List<GitHubTeamRepository> {
        val lens = getLens<List<GitHubTeamRepository>>()
        val request = GetRequest("/orgs/$organizationName/teams/$teamSlug/repos")
        val response = client(request)

        return when (response.status) {
            Status.OK -> lens(response)
            Status.NOT_FOUND -> emptyList()
            // TODO Deal with unexpected responses
            else -> emptyList()
        }
    }
}