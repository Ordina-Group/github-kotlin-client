package com.soprasteria.github.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepository(
    val owner: String,
    val id: Int,
    val name: String,
    @SerialName("full_name") val fullName: String,
)