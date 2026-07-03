package com.soprasteria.github

import com.soprasteria.github.team.GitHubTeamMember
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

class GitHubTeamSpec :
    WordSpec({

        val httpClient = mockk<HttpHandler>()
        val client = GitHubClient(httpClient)
        val team = Defaults.team()

        "teams.getMembers" should {

            "return Found with an empty list when the team has no members" {
                every {
                    httpClient.invoke(
                        matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/members?page=1&per_page=100"),
                    )
                }.returns(Response(Status.OK).body("[]"))

                val result = client.teams.getMembers(team)

                result.shouldBeInstanceOf<ApiResult.Found<List<GitHubTeamMember>>>()
                result.getOrThrow() shouldBe emptyList()
            }

            "return Found with members when the team has members" {
                every {
                    httpClient.invoke(
                        matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/members?page=1&per_page=100"),
                    )
                }.returns(Response(Status.OK).body(Json.encodeToString(listOf(Defaults.teamMember()))))

                val result = client.teams.getMembers(team)

                result.shouldBeInstanceOf<ApiResult.Found<List<GitHubTeamMember>>>()
                result.getOrThrow().size shouldBe 1
            }

            "return Failure when the API returns a server error" {
                every {
                    httpClient.invoke(
                        matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/members?page=1&per_page=100"),
                    )
                }.returns(Response(Status.INTERNAL_SERVER_ERROR))

                val result = client.teams.getMembers(team)

                result.shouldBeInstanceOf<ApiResult.Failure>()
                (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
            }
        }

        "teams.addMember" should {

            "return Found(Unit) when the member is added successfully" {
                every { httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/memberships/octocat")) }
                    .returns(Response(Status.OK).body("{}"))

                val result = client.teams.addMember(team, "octocat")

                result.shouldBeInstanceOf<ApiResult.Found<Unit>>()
                result.getOrThrow() shouldBe Unit
            }

            "return NotFound when the team or org does not exist" {
                every { httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/memberships/octocat")) }
                    .returns(Response(Status.NOT_FOUND))

                val result = client.teams.addMember(team, "octocat")

                result.shouldBeInstanceOf<ApiResult.NotFound>()
            }

            "return Failure when the API returns a server error" {
                every { httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/memberships/octocat")) }
                    .returns(Response(Status.INTERNAL_SERVER_ERROR))

                val result = client.teams.addMember(team, "octocat")

                result.shouldBeInstanceOf<ApiResult.Failure>()
                (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
            }
        }

        "teams.removeMember" should {

            "return Found(Unit) when the member is removed successfully" {
                every { httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/memberships/octocat")) }
                    .returns(Response(Status.NO_CONTENT).body(""))

                val result = client.teams.removeMember(team, "octocat")

                result.shouldBeInstanceOf<ApiResult.Found<Unit>>()
                result.getOrThrow() shouldBe Unit
            }

            "return NotFound when the membership does not exist" {
                every { httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/memberships/octocat")) }
                    .returns(Response(Status.NOT_FOUND))

                val result = client.teams.removeMember(team, "octocat")

                result.shouldBeInstanceOf<ApiResult.NotFound>()
            }

            "return Failure when the API returns a server error" {
                every { httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/memberships/octocat")) }
                    .returns(Response(Status.INTERNAL_SERVER_ERROR))

                val result = client.teams.removeMember(team, "octocat")

                result.shouldBeInstanceOf<ApiResult.Failure>()
                (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
            }
        }

        "teams.getRepositories" should {

            "return Found with an empty list when the team has no repositories" {
                every {
                    httpClient.invoke(
                        matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/repos?page=1&per_page=100"),
                    )
                }.returns(Response(Status.OK).body("[]"))

                val result = client.teams.getRepositories(team)

                result.shouldBeInstanceOf<ApiResult.Found<*>>()
                result.getOrThrow() shouldBe emptyList()
            }
        }

        "teams.addRepository" should {

            "return Found(Unit) when the repository is added successfully" {
                every {
                    httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/repos/${Defaults.OWNER}/${Defaults.repository().name}"))
                }.returns(Response(Status.NO_CONTENT).body(""))

                val result = client.teams.addRepository(team, Defaults.repository().name, com.soprasteria.github.repository.Permission.Push)

                result.shouldBeInstanceOf<ApiResult.Found<Unit>>()
                result.getOrThrow() shouldBe Unit
            }

            "return NotFound when the team or repository does not exist" {
                every {
                    httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/repos/${Defaults.OWNER}/${Defaults.repository().name}"))
                }.returns(Response(Status.NOT_FOUND))

                val result = client.teams.addRepository(team, Defaults.repository().name, com.soprasteria.github.repository.Permission.Push)

                result.shouldBeInstanceOf<ApiResult.NotFound>()
            }

            "return Failure when the API returns a server error" {
                every {
                    httpClient.invoke(matchUri("/orgs/${Defaults.OWNER}/teams/${team.slug}/repos/${Defaults.OWNER}/${Defaults.repository().name}"))
                }.returns(Response(Status.INTERNAL_SERVER_ERROR))

                val result = client.teams.addRepository(team, Defaults.repository().name, com.soprasteria.github.repository.Permission.Push)

                result.shouldBeInstanceOf<ApiResult.Failure>()
                (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
            }
        }
    })