package com.soprasteria.github

import com.soprasteria.github.internal.GitHubRepositoryClient
import com.soprasteria.github.internal.GitHubRepositoryClient.Affiliation
import com.soprasteria.github.repository.GitHubRepository
import com.soprasteria.github.repository.GitHubRepositoryCollaborator
import com.soprasteria.github.repository.GitHubRepositoryContributor
import com.soprasteria.github.repository.GitHubRepositoryTeam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.http4k.core.HttpHandler

/**
 * Operations on GitHub repositories.
 * Obtain via [GitHubClient.repositories].
 */
class RepositoryService internal constructor(
    httpClient: HttpHandler,
) {
    private val repoClient = GitHubRepositoryClient(httpClient)

    /** Fetches a single repository. Returns [ApiResult.NotFound] for HTTP 404. */
    suspend fun get(
        owner: String,
        name: String,
    ): ApiResult<GitHubRepository> =
        withContext(Dispatchers.IO) {
            apiResult { repoClient.getRepository(owner, name) }
        }

    /** Lists all teams with explicit access to a repository. */
    suspend fun getTeams(
        owner: String,
        repositoryName: String,
    ): ApiResult<List<GitHubRepositoryTeam>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getTeams(owner, repositoryName) }
        }

    /** @see getTeams */
    suspend fun getTeams(repository: GitHubRepository): ApiResult<List<GitHubRepositoryTeam>> = getTeams(repository.owner, repository.name)

    /** Lists all collaborators (direct + outside). */
    suspend fun getAllCollaborators(
        owner: String,
        repositoryName: String,
    ): ApiResult<List<GitHubRepositoryCollaborator>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getCollaborators(owner, repositoryName, Affiliation.ALL) }
        }

    /** @see getAllCollaborators */
    suspend fun getAllCollaborators(repository: GitHubRepository): ApiResult<List<GitHubRepositoryCollaborator>> =
        getAllCollaborators(repository.owner, repository.name)

    /** Lists collaborators added directly to the repository. */
    suspend fun getDirectCollaborators(
        owner: String,
        repositoryName: String,
    ): ApiResult<List<GitHubRepositoryCollaborator>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getCollaborators(owner, repositoryName, Affiliation.DIRECT) }
        }

    /** @see getDirectCollaborators */
    suspend fun getDirectCollaborators(repository: GitHubRepository): ApiResult<List<GitHubRepositoryCollaborator>> =
        getDirectCollaborators(repository.owner, repository.name)

    /** Lists collaborators who are not org members (outside collaborators). */
    suspend fun getOutsideCollaborators(
        owner: String,
        repositoryName: String,
    ): ApiResult<List<GitHubRepositoryCollaborator>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getCollaborators(owner, repositoryName, Affiliation.OUTSIDE) }
        }

    /** @see getOutsideCollaborators */
    suspend fun getOutsideCollaborators(repository: GitHubRepository): ApiResult<List<GitHubRepositoryCollaborator>> =
        getOutsideCollaborators(repository.owner, repository.name)

    /** Transfers a repository to a new owner. Returns [ApiResult.NotFound] if the repository does not exist. */
    suspend fun transfer(
        owner: String,
        repositoryName: String,
        newOwner: String,
    ): ApiResult<Unit> =
        withContext(Dispatchers.IO) {
            apiResult { repoClient.transfer(owner, repositoryName, newOwner) }
        }

    /** @see transfer */
    suspend fun transfer(
        repository: GitHubRepository,
        newOwner: String,
    ): ApiResult<Unit> = transfer(repository.owner, repository.name, newOwner)

    /** Lists top contributors to a repository (up to maxContributors, defaults to 5). */
    suspend fun getContributors(
        owner: String,
        repositoryName: String,
        maxContributors: Int = 5,
    ): ApiResult<List<GitHubRepositoryContributor>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getContributors(owner, repositoryName, maxContributors) }
        }

    /** @see getContributors */
    suspend fun getContributors(
        repository: GitHubRepository,
        maxContributors: Int = 5,
    ): ApiResult<List<GitHubRepositoryContributor>> = getContributors(repository.owner, repository.name, maxContributors)

    /** Lists all contributors to a repository (paginated). */
    suspend fun getAllContributors(
        owner: String,
        repositoryName: String,
    ): ApiResult<List<GitHubRepositoryContributor>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getAllContributors(owner, repositoryName) }
        }

    /** @see getAllContributors */
    suspend fun getAllContributors(repository: GitHubRepository): ApiResult<List<GitHubRepositoryContributor>> =
        getAllContributors(repository.owner, repository.name)
}