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

    "A GitHub organization listing its repositories" When {

        "the organization has no repositories" should {
            val httpClient = mockk<HttpHandler>()
            val organization = Defaults.organization(httpClient)

            every { httpClient.invoke(matchUri("orgs/${organization.login}/repos?page=1&per_page=100")) }
                .returns(Response(Status.OK).body("[]"))

            organization.getRepositories().shouldBeEmpty()
        }

        "the organization has repositories" should {
            val httpClient = mockk<HttpHandler>()
            val organization = Defaults.organization(httpClient)
            val repository = Defaults.repository(httpClient)

            every { httpClient.invoke(matchUri("orgs/${organization.login}/repos?page=1&per_page=100")) }
                .returns(Response(Status.OK).body(Json.encodeToString(listOf(repository))))

            organization.getRepositories() shouldHaveSize 1
        }
    }
})