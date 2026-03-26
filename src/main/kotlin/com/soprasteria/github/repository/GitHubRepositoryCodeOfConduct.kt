package com.soprasteria.github.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryCodeOfConduct(
    val key: String,
    val name: String,
    val url: String,
    val body: String,
    @SerialName("html_url") val htmlUrl: String?,
)