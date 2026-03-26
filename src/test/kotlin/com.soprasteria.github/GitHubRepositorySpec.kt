package com.soprasteria.github

import com.soprasteria.github.repository.GitHubRepositoryTeam
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

class GitHubRepositorySpec :
    WordSpec({

        val httpClient = mockk<HttpHandler>()
        val client = GitHubClient(httpClient)
        val repo = Defaults.repository()

        "repositories.getTeams" should {

            "return Found with an empty list when the repository has no teams" {
                every { httpClient.invoke(matchUri("/repos/${repo.owner}/${repo.name}/teams")) }
                    .returns(Response(Status.OK).body("[]"))

                val result = client.repositories.getTeams(repo)

                result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepositoryTeam>>>()
                result.getOrThrow() shouldBe emptyList()
            }

            "return Failure when the API returns a server error" {
                every { httpClient.invoke(matchUri("/repos/${repo.owner}/${repo.name}/teams")) }
                    .returns(Response(Status.INTERNAL_SERVER_ERROR))

                val result = client.repositories.getTeams(repo)

                result.shouldBeInstanceOf<ApiResult.Failure>()
                (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
            }
        }
    })