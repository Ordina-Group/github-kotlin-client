package nl.ordina.github

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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

    "A GitHub team" should {

        "return an empty list when the team has no members" {
            val httpClient = mockk<HttpHandler>()
            val team = Defaults.team(httpClient)

            every { httpClient.invoke(matchUri("orgs/${Defaults.owner}/teams/${team.slug}/members?page=1&per_page=100")) }
                .returns(Response(Status.OK).body("[]"))

            team.getMembers().shouldBeEmpty()
        }

        "return members when the team has members" {
            val httpClient = mockk<HttpHandler>()
            val team = Defaults.team(httpClient)

            every { httpClient.invoke(matchUri("orgs/${Defaults.owner}/teams/${team.slug}/members?page=1&per_page=100")) }
                .returns(Response(Status.OK).body(Json.encodeToString(listOf(Defaults.teamMember()))))

            team.getMembers() shouldHaveSize 1
        }

        "add a member to the team" {
            val httpClient = mockk<HttpHandler>()
            val team = Defaults.team(httpClient)

            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/memberships/octocat")) }
                .returns(Response(Status.OK).body("{}"))

            team.addMember("octocat")

            verify { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/memberships/octocat")) }
        }

        "remove a member from the team" {
            val httpClient = mockk<HttpHandler>()
            val team = Defaults.team(httpClient)

            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/memberships/octocat")) }
                .returns(Response(Status.NO_CONTENT).body(""))

            team.removeMember("octocat")

            verify { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/memberships/octocat")) }
        }

        "return an empty list when the team has no repositories" {
            val httpClient = mockk<HttpHandler>()
            val team = Defaults.team(httpClient)

            every { httpClient.invoke(matchUri("/orgs/${Defaults.owner}/teams/${team.slug}/repos?page=1&per_page=100")) }
                .returns(Response(Status.OK).body("[]"))

            team.getRepositories().shouldBeEmpty()
        }
    }

    "A GitHub team encountering API errors" should {

        "throw GitHubApiException when getting members returns a server error" {
            val httpClient = mockk<HttpHandler>()
            val team = Defaults.team(httpClient)

            every { httpClient.invoke(matchUri("orgs/${Defaults.owner}/teams/${team.slug}/members?page=1&per_page=100")) }
                .returns(Response(Status.INTERNAL_SERVER_ERROR))

            val exception = shouldThrow<GitHubApiException> { team.getMembers() }
            exception.status shouldBe Status.INTERNAL_SERVER_ERROR
        }
    }
})
