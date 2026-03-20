package nl.ordina.github

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.github.team.GitHubTeamMember
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

class GitHubTeamSpec : WordSpec({

    val httpClient = mockk<HttpHandler>()
    val client = GitHubClient(httpClient)
    val team = Defaults.team()

    "teams.getMembers" should {

        "return Found with an empty list when the team has no members" {
            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/members?page=1&per_page=100")) }
                .returns(Response(Status.OK).body("[]"))

            val result = client.teams.getMembers(team)

            result.shouldBeInstanceOf<ApiResult.Found<List<GitHubTeamMember>>>()
            result.getOrThrow() shouldBe emptyList()
        }

        "return Found with members when the team has members" {
            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/members?page=1&per_page=100")) }
                .returns(Response(Status.OK).body(Json.encodeToString(listOf(Defaults.teamMember()))))

            val result = client.teams.getMembers(team)

            result.shouldBeInstanceOf<ApiResult.Found<List<GitHubTeamMember>>>()
            result.getOrThrow().size shouldBe 1
        }

        "return Failure when the API returns a server error" {
            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/members?page=1&per_page=100")) }
                .returns(Response(Status.INTERNAL_SERVER_ERROR))

            val result = client.teams.getMembers(team)

            result.shouldBeInstanceOf<ApiResult.Failure>()
            (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
        }
    }

    "teams.addMember" should {

        "call the correct endpoint" {
            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/memberships/octocat")) }
                .returns(Response(Status.OK).body("{}"))

            client.teams.addMember(team, "octocat")

            verify { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/memberships/octocat")) }
        }
    }

    "teams.removeMember" should {

        "call the correct endpoint" {
            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/memberships/octocat")) }
                .returns(Response(Status.NO_CONTENT).body(""))

            client.teams.removeMember(team, "octocat")

            verify { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/memberships/octocat")) }
        }
    }

    "teams.getRepositories" should {

        "return Found with an empty list when the team has no repositories" {
            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/repos?page=1&per_page=100")) }
                .returns(Response(Status.OK).body("[]"))

            val result = client.teams.getRepositories(team)

            result.shouldBeInstanceOf<ApiResult.Found<*>>()
            result.getOrThrow() shouldBe emptyList()
        }
    }
})
