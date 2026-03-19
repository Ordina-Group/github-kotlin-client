package nl.ordina.github

import nl.ordina.github.internal.GitHubOrganizationClient
import nl.ordina.github.internal.GitHubRepositoryClient
import nl.ordina.github.internal.GitHubTeamClient
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.team.GitHubTeam
import nl.ordina.github.team.GitHubTeamMember
import org.http4k.core.HttpHandler

object Defaults {
    const val owner = "github"

    fun organization(httpClient: HttpHandler) = GitHubOrganization(
        login = owner,
        id = 1,
        nodeId = "MDEyOk9yZ2FuaXphdGlvbjE=",
        name = "github",
        company = "GitHub"
    ).also { it.organizationClient = GitHubOrganizationClient(httpClient) }

    fun repository(httpClient: HttpHandler) = GitHubRepository(
        owner = owner,
        id = 1,
        name = "Mona-Liza",
        fullName = "github/Mona-Liza"
    ).also { it.repositoryClient = GitHubRepositoryClient(httpClient) }

    fun team(httpClient: HttpHandler) = GitHubTeam(
        organization = owner,
        id = 1,
        nodeId = "T_kgDOA",
        name = "Justice League",
        slug = "justice-league",
        description = "The best team",
        privacy = "secret",
        notificationSetting = "notifications_enabled",
        permission = "pull",
        permissions = null,
        url = "https://api.github.com/orgs/$owner/teams/justice-league",
        htmlUrl = "https://github.com/orgs/$owner/teams/justice-league",
        membersUrl = "https://api.github.com/orgs/$owner/teams/justice-league/members{/member}",
        repositoriesUrl = "https://api.github.com/orgs/$owner/teams/justice-league/repos",
        parent = null
    ).also { it.teamClient = GitHubTeamClient(httpClient) }

    fun teamMember() = GitHubTeamMember(
        login = "octocat",
        id = 1,
        nodeId = "MDQ6VXNlcjE=",
        avatarUrl = "https://github.com/images/error/octocat_happy.gif",
        url = "https://api.github.com/users/octocat",
        htmlUrl = "https://github.com/octocat",
        followersUrl = "https://api.github.com/users/octocat/followers",
        followingUrl = "https://api.github.com/users/octocat/following{/other_user}",
        gistsUrl = "https://api.github.com/users/octocat/gists{/gist_id}",
        starredUrl = "https://api.github.com/users/octocat/starred{/owner}{/repo}",
        subscriptionsUrl = "https://api.github.com/users/octocat/subscriptions",
        organizationsUrl = "https://api.github.com/users/octocat/orgs",
        reposUrl = "https://api.github.com/users/octocat/repos",
        eventsUrl = "https://api.github.com/users/octocat/events{/privacy}",
        receivedEventsUrl = "https://api.github.com/users/octocat/received_events",
        type = "User",
        siteAdmin = false
    )
}