package nl.ordina.github

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.ordina.github.internal.GitHubRepositoryClient
import nl.ordina.github.internal.GitHubRepositoryClient.Affiliation
import nl.ordina.github.repository.GitHubRepository
import nl.ordina.github.repository.GitHubRepositoryCollaborator
import nl.ordina.github.repository.GitHubRepositoryTeam
import org.http4k.core.HttpHandler

/**
 * Operations on GitHub repositories.
 * Obtain via [GitHubClient.repositories].
 */
class RepositoryService internal constructor(httpClient: HttpHandler) {
    private val repoClient = GitHubRepositoryClient(httpClient)

    /** Fetches a single repository. Returns [ApiResult.NotFound] for HTTP 404. */
    suspend fun get(owner: String, name: String): ApiResult<GitHubRepository> = withContext(Dispatchers.IO) {
        apiResult { repoClient.getRepository(owner, name) }
    }

    /** Lists all teams with explicit access to a repository. */
    suspend fun getTeams(owner: String, repositoryName: String): ApiResult<List<GitHubRepositoryTeam>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getTeams(owner, repositoryName) }
        }

    /** @see getTeams */
    suspend fun getTeams(repository: GitHubRepository): ApiResult<List<GitHubRepositoryTeam>> =
        getTeams(repository.owner, repository.name)

    /** Lists all collaborators (direct + outside). */
    suspend fun getAllCollaborators(owner: String, repositoryName: String): ApiResult<List<GitHubRepositoryCollaborator>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getCollaborators(owner, repositoryName, Affiliation.ALL) }
        }

    /** @see getAllCollaborators */
    suspend fun getAllCollaborators(repository: GitHubRepository): ApiResult<List<GitHubRepositoryCollaborator>> =
        getAllCollaborators(repository.owner, repository.name)

    /** Lists collaborators added directly to the repository. */
    suspend fun getDirectCollaborators(owner: String, repositoryName: String): ApiResult<List<GitHubRepositoryCollaborator>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getCollaborators(owner, repositoryName, Affiliation.DIRECT) }
        }

    /** @see getDirectCollaborators */
    suspend fun getDirectCollaborators(repository: GitHubRepository): ApiResult<List<GitHubRepositoryCollaborator>> =
        getDirectCollaborators(repository.owner, repository.name)

    /** Lists collaborators who are not org members (outside collaborators). */
    suspend fun getOutsideCollaborators(owner: String, repositoryName: String): ApiResult<List<GitHubRepositoryCollaborator>> =
        withContext(Dispatchers.IO) {
            apiListResult { repoClient.getCollaborators(owner, repositoryName, Affiliation.OUTSIDE) }
        }

    /** @see getOutsideCollaborators */
    suspend fun getOutsideCollaborators(repository: GitHubRepository): ApiResult<List<GitHubRepositoryCollaborator>> =
        getOutsideCollaborators(repository.owner, repository.name)

    /** Transfers a repository to a new owner. */
    suspend fun transfer(owner: String, repositoryName: String, newOwner: String) = withContext(Dispatchers.IO) {
        repoClient.transfer(owner, repositoryName, newOwner)
    }

    /** @see transfer */
    suspend fun transfer(repository: GitHubRepository, newOwner: String) =
        transfer(repository.owner, repository.name, newOwner)
}
