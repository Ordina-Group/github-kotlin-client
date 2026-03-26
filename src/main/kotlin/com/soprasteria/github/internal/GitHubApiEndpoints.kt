package com.soprasteria.github.internal

/**
 * Typed URL builders for all GitHub API endpoints used by this library.
 * Eliminates hard-coded string interpolation scattered across client classes.
 */
internal object GitHubApiEndpoints {
    // Organizations
    fun organization(org: String) = "/orgs/$org"

    fun organizationMembers(org: String) = "/orgs/$org/members"

    fun organizationInvitations(org: String) = "/orgs/$org/invitations"

    fun organizationRepositories(org: String) = "/orgs/$org/repos"

    // Teams
    fun organizationTeams(org: String) = "/orgs/$org/teams"

    fun organizationTeam(
        org: String,
        slug: String,
    ) = "/orgs/$org/teams/$slug"

    fun organizationTeamMembers(
        org: String,
        slug: String,
    ) = "/orgs/$org/teams/$slug/members"

    fun organizationTeamMembership(
        org: String,
        slug: String,
        username: String,
    ) = "/orgs/$org/teams/$slug/memberships/$username"

    fun organizationTeamRepositories(
        org: String,
        slug: String,
    ) = "/orgs/$org/teams/$slug/repos"

    /**
     * @param org       the organization that owns the team
     * @param slug      the team slug
     * @param repoOwner the owner of the repository (may differ from [org] for forks or user repos)
     * @param repo      the repository name
     */
    fun organizationTeamRepository(
        org: String,
        slug: String,
        repoOwner: String,
        repo: String,
    ) = "/orgs/$org/teams/$slug/repos/$repoOwner/$repo"

    // Repositories
    fun repository(
        owner: String,
        repo: String,
    ) = "/repos/$owner/$repo"

    fun repositoryTeams(
        owner: String,
        repo: String,
    ) = "/repos/$owner/$repo/teams"

    fun repositoryCollaborators(
        owner: String,
        repo: String,
    ) = "/repos/$owner/$repo/collaborators"

    fun repositoryTransfer(
        owner: String,
        repo: String,
    ) = "/repos/$owner/$repo/transfer"
}