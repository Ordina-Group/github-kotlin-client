package nl.ordina.github

/**
 * Represents the outcome of a GitHub API call for a single resource.
 *
 * - [Found] — the resource exists and [Found.value] holds the data.
 * - [NotFound] — the resource does not exist (HTTP 404).
 * - [Failure] — the API returned an unexpected error.
 */
sealed class ApiResult<out T> {
    data class Found<out T>(val value: T) : ApiResult<T>()
    data object NotFound : ApiResult<Nothing>()
    data class Failure(val exception: GitHubApiException) : ApiResult<Nothing>()

    /** Returns the value when [Found], or `null` for [NotFound] and [Failure]. */
    fun getOrNull(): T? = (this as? Found)?.value

    /**
     * Returns the value when [Found].
     * Throws [GitHubApiException] on [Failure] or [NoSuchElementException] on [NotFound].
     */
    fun getOrThrow(): T = when (this) {
        is Found -> value
        is NotFound -> throw NoSuchElementException("Resource not found")
        is Failure -> throw exception
    }

    fun isFound(): Boolean = this is Found
    fun isNotFound(): Boolean = this == NotFound
    fun isFailure(): Boolean = this is Failure
}
