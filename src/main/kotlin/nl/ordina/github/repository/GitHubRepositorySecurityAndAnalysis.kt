package nl.ordina.github.repository

import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    Enabled,
    Disabled
}

@Serializable
data class SecuritySetting(val status: Status) {
    val isEnabled: Boolean = status == Status.Enabled
    val isDisabled: Boolean = status == Status.Disabled
}

@Serializable
data class GitHubRepositorySecurityAndAnalysis(
    val advanced_security: SecuritySetting,
    val secret_scanning: SecuritySetting,
    val secret_scanning_push_protection: SecuritySetting
)