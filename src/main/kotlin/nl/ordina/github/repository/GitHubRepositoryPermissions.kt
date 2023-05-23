package nl.ordina.github.repository

import kotlinx.serialization.Serializable

enum class Permission(val value: String) {
    Admin("admin"),
    Maintain("maintain"),
    Push("push"),
    Triage("triage"),
    Pull("pull")

}
@Serializable
data class GitHubRepositoryPermissions internal constructor(
    val pull: Boolean? = null,
    val triage: Boolean? = null,
    val push: Boolean? = null,
    val maintain: Boolean? = null,
    val admin: Boolean? = null
) {
    val permission: Permission
        get() {
            return if (admin == true) {
                Permission.Admin
            } else if (maintain == true) {
                Permission.Maintain
            } else if (push == true) {
                Permission.Push
            } else if (triage == true) {
                Permission.Triage
            } else {
                Permission.Pull
            }
        }
}
