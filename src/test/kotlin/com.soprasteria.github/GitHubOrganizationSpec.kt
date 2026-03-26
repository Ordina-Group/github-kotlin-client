package com.soprasteria.github

import com.soprasteria.github.repository.GitHubRepository
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

class GitHubOrganizationSpec :
    WordSpec({

        val httpClient = mockk<HttpHandler>()
        val client = GitHubClient(httpClient)
        val org = Defaults.organization()

        "organizations.getRepositories" When {

            "the organization has no repositories" should {
                "return Found with an empty list" {
                    every { httpClient.invoke(matchUri("/orgs/${org.login}/repos?page=1&per_page=100")) }
                        .returns(Response(Status.OK).body("[]"))

                    val result = client.organizations.getRepositories(org)

                    result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepository>>>()
                    result.getOrThrow() shouldBe emptyList()
                }
            }

            "the organization has repositories" should {
                "return Found with all repositories" {
                    every { httpClient.invoke(matchUri("/orgs/${org.login}/repos?page=1&per_page=100")) }
                        .returns(Response(Status.OK).body(Json.encodeToString(listOf(Defaults.repository()))))

                    val result = client.organizations.getRepositories(org)

                    result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepository>>>()
                    result.getOrThrow().size shouldBe 1
                }
            }
        }
    })