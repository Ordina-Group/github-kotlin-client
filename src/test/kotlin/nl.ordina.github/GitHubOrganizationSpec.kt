package nl.ordina.github

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

class GitHubOrganizationSpec : WordSpec({

    "A GitHub organization" should {
        val httpClient = mockk<HttpHandler>()

        "be able to list repositories belonging to the organization when the organization has no repositories" {
            GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("orgs/${Defaults.organization.login}/repos")) }
                .returns(Response(Status.OK).body("[]"))

            val repositories = Defaults.organization.getRepositories()

            repositories.shouldBeEmpty()
        }

        "be able to list repositories belonging to the organization when the organization has repositories" {
            GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("orgs/${Defaults.organization.login}/repos")) }
                .returns(Response(Status.OK).body(Json.encodeToString(listOf(Defaults.repository))))

            val repositories = Defaults.organization.getRepositories()

            repositories shouldHaveSize 1
        }
    }
})