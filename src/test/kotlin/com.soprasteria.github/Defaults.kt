package com.soprasteria.github

import com.soprasteria.github.organization.GitHubOrganization
import com.soprasteria.github.repository.GitHubRepository
import com.soprasteria.github.repository.GitHubRepositoryContributor
import com.soprasteria.github.repository.GitHubRepositoryTeam
import com.soprasteria.github.team.GitHubTeam
import com.soprasteria.github.team.GitHubTeamMember

object Defaults {
    const val OWNER = "github"

    fun organization() =
        GitHubOrganization(
            login = OWNER,
            id = 1,
            nodeId = "MDEyOk9yZ2FuaXphdGlvbjE=",
            name = "github",
            company = "GitHub",
        )

    fun repository() =
        GitHubRepository(
            owner = OWNER,
            id = 1,
            name = "Mona-Liza",
            fullName = "github/Mona-Liza",
        )

    fun contributor(
        login: String = "octocat",
        id: Int = 1,
        contributions: Int = 42,
    ) = GitHubRepositoryContributor(
        login = login,
        id = id,
        nodeId = "MDQ6VXNlcjE=",
        avatarUrl = "https://github.com/images/error/$login.gif",
        gravatarId = null,
        url = "https://api.github.com/users/$login",
        htmlUrl = "https://github.com/$login",
        followersUrl = "https://api.github.com/users/$login/followers",
        followingUrl = "https://api.github.com/users/$login/following{/other_user}",
        gistsUrl = "https://api.github.com/users/$login/gists{/gist_id}",
        starredUrl = "https://api.github.com/users/$login/starred{/owner}{/repo}",
        subscriptionsUrl = "https://api.github.com/users/$login/subscriptions",
        organizationsUrl = "https://api.github.com/users/$login/orgs",
        reposUrl = "https://api.github.com/users/$login/repos",
        eventsUrl = "https://api.github.com/users/$login/events{/privacy}",
        receivedEventsUrl = "https://api.github.com/users/$login/received_events",
        type = "User",
        siteAdmin = false,
        contributions = contributions,
    )

    fun repositoryTeam(
        organization: String = OWNER,
        id: Int = 1,
        name: String = "Justice League",
        slug: String = "justice-league",
        description: String = "The best team",
        privacy: String = "secret",
        permission: String = "pull",
    ) = GitHubRepositoryTeam(
        organization = organization,
        id = id,
        nodeId = "T_kwDOA$id",
        url = "https://api.github.com/teams/$id",
        htmlUrl = "https://github.com/orgs/$organization/teams/$slug",
        name = name,
        slug = slug,
        description = description,
        privacy = privacy,
        notificationSetting = "notifications_enabled",
        permission = permission,
        membersUrl = "https://api.github.com/teams/$id/members{/member}",
        repositoriesUrl = "https://api.github.com/teams/$id/repos",
    )

    fun team() =
        GitHubTeam(
            organization = OWNER,
            id = 1,
            nodeId = "T_kgDOA",
            name = "Justice League",
            slug = "justice-league",
            description = "The best team",
            privacy = "secret",
            notificationSetting = "notifications_enabled",
            permission = "pull",
            permissions = null,
            url = "https://api.github.com/orgs/$OWNER/teams/justice-league",
            htmlUrl = "https://github.com/orgs/$OWNER/teams/justice-league",
            membersUrl = "https://api.github.com/orgs/$OWNER/teams/justice-league/members{/member}",
            repositoriesUrl = "https://api.github.com/orgs/$OWNER/teams/justice-league/repos",
            parent = null,
        )

    fun teamMember() =
        GitHubTeamMember(
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
            siteAdmin = false,
        )
}