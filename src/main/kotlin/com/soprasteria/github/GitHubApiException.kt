package com.soprasteria.github

import org.http4k.core.Response
import org.http4k.core.Status

/**
 * Thrown when the GitHub API returns an unexpected response.
 *
 * @param status The HTTP status code returned by the API
 * @param message A human-readable description of the error
 */
class GitHubApiException(
    val status: Status,
    message: String
) : RuntimeException(message) {
    companion object {
        fun from(response: Response, context: String): GitHubApiException =
            GitHubApiException(
                status = response.status,
                message = "GitHub API error during $context: HTTP ${response.status.code} ${response.status.description}"
            )
    }
}
