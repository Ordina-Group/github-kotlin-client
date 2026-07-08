package com.soprasteria.github

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
    message: String,
) : RuntimeException(message) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun from(
            response: Response,
            context: String,
        ): GitHubApiException =
            GitHubApiException(
                status = response.status,
                message = buildMessage(response, context),
            )

        private fun buildMessage(
            response: Response,
            context: String,
        ): String {
            val details = extractErrorMessage(response.bodyString())
            val reason = details?.let { ": $it" }.orEmpty()
            return "GitHub API error during $context: HTTP ${response.status.code} ${response.status.description}$reason"
        }

        private fun extractErrorMessage(body: String): String? {
            val trimmedBody = body.trim()
            if (trimmedBody.isEmpty()) {
                return null
            }

            return parseErrorResponse(trimmedBody)?.message?.takeIf { it.isNotBlank() } ?: trimmedBody
        }

        private fun parseErrorResponse(body: String): GitHubErrorResponse? =
            try {
                json.decodeFromString<GitHubErrorResponse>(body)
            } catch (_: SerializationException) {
                null
            }
    }

    @Serializable
    private data class GitHubErrorResponse(
        val message: String? = null,
    )
}