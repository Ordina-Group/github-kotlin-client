package com.soprasteria.github

import com.soprasteria.github.repository.GitHubRepositoryContributor
import com.soprasteria.github.repository.GitHubRepositoryTeam
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

        "repositories.transfer" should {

            "return Found(Unit) when the transfer is accepted" {
                every { httpClient.invoke(matchUri("/repos/${repo.owner}/${repo.name}/transfer")) }
                    .returns(Response(Status.ACCEPTED).body("{}"))

                val result = client.repositories.transfer(repo, newOwner = "new-org")

                result.shouldBeInstanceOf<ApiResult.Found<Unit>>()
                result.getOrThrow() shouldBe Unit
            }

            "return NotFound when the repository does not exist" {
                every { httpClient.invoke(matchUri("/repos/${repo.owner}/${repo.name}/transfer")) }
                    .returns(Response(Status.NOT_FOUND))

                val result = client.repositories.transfer(repo, newOwner = "new-org")

                result.shouldBeInstanceOf<ApiResult.NotFound>()
            }

            "return Failure when the API returns a server error" {
                every { httpClient.invoke(matchUri("/repos/${repo.owner}/${repo.name}/transfer")) }
                    .returns(Response(Status.INTERNAL_SERVER_ERROR))

                val result = client.repositories.transfer(repo, newOwner = "new-org")

                result.shouldBeInstanceOf<ApiResult.Failure>()
                (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
            }
        }

        "repositories.getAllContributors" should {

            "return Found with contributors when a single page is returned" {
                val contributor = Defaults.contributor()

                every {
                    httpClient.invoke(
                        matchUri("/repos/${repo.owner}/${repo.name}/contributors?page=1&per_page=100"),
                    )
                }.returns(Response(Status.OK).body(Json.encodeToString(listOf(contributor))))

                val result = client.repositories.getAllContributors(repo)

                result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepositoryContributor>>>()
                result.getOrThrow() shouldBe listOf(contributor)
            }

            "follow Link rel=next headers across multiple pages" {
                val firstContributor = Defaults.contributor(login = "octocat", id = 1, contributions = 42)
                val secondContributor = Defaults.contributor(login = "hubot", id = 2, contributions = 7)
                val linkHeader =
                    """<https://api.github.com/repos/${repo.owner}/${repo.name}/contributors?page=2>; rel="next""""

                every {
                    httpClient.invoke(
                        matchUri("/repos/${repo.owner}/${repo.name}/contributors?page=1&per_page=100"),
                    )
                }.returns(
                    Response(Status.OK)
                        .header("Link", linkHeader)
                        .body(Json.encodeToString(listOf(firstContributor))),
                )
                every {
                    httpClient.invoke(
                        matchUri("/repos/${repo.owner}/${repo.name}/contributors?page=2&per_page=100"),
                    )
                }.returns(Response(Status.OK).body(Json.encodeToString(listOf(secondContributor))))

                val result = client.repositories.getAllContributors(repo)

                result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepositoryContributor>>>()
                result.getOrThrow() shouldBe listOf(firstContributor, secondContributor)
            }

            "return Found with an empty list when contributors are not available" {
                every {
                    httpClient.invoke(
                        matchUri("/repos/${repo.owner}/${repo.name}/contributors?page=1&per_page=100"),
                    )
                }.returns(Response(Status.NOT_FOUND))

                val result = client.repositories.getAllContributors(repo)

                result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepositoryContributor>>>()
                result.getOrThrow() shouldBe emptyList()
            }

            "return contributors collected before a later page fails" {
                val contributor = Defaults.contributor()
                val linkHeader =
                    """<https://api.github.com/repos/${repo.owner}/${repo.name}/contributors?page=2>; rel="next""""

                every {
                    httpClient.invoke(
                        matchUri("/repos/${repo.owner}/${repo.name}/contributors?page=1&per_page=100"),
                    )
                }.returns(
                    Response(Status.OK)
                        .header("Link", linkHeader)
                        .body(Json.encodeToString(listOf(contributor))),
                )
                every {
                    httpClient.invoke(
                        matchUri("/repos/${repo.owner}/${repo.name}/contributors?page=2&per_page=100"),
                    )
                }.returns(Response(Status.INTERNAL_SERVER_ERROR))

                val result = client.repositories.getAllContributors(repo)

                result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepositoryContributor>>>()
                result.getOrThrow() shouldBe listOf(contributor)
            }
        }
    })