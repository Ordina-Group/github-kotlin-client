package nl.ordina.github

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

class GitHubRepositorySpec : WordSpec({

    "A GitHub repository" should {

        "return an empty list when the repository has no teams" {
            val httpClient = mockk<HttpHandler>()
            val repository = Defaults.repository(httpClient)

            every { httpClient.invoke(matchUri("/repos/${Defaults.owner}/${repository.name}/teams")) } returns
                Response(Status.OK).body("[]")

            repository.getTeams().shouldBeEmpty()
        }
    }

    "A GitHub repository encountering API errors" should {

        "throw GitHubApiException when the API returns a server error for getTeams" {
            val httpClient = mockk<HttpHandler>()
            val repository = Defaults.repository(httpClient)

            every { httpClient.invoke(matchUri("/repos/${Defaults.owner}/${repository.name}/teams")) } returns
                Response(Status.INTERNAL_SERVER_ERROR)

            val exception = shouldThrow<GitHubApiException> { repository.getTeams() }
            exception.status shouldBe Status.INTERNAL_SERVER_ERROR
        }
    }
})