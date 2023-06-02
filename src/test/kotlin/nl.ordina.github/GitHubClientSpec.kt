package nl.ordina.github

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.repository.GitHubRepository
import org.http4k.core.*
import org.http4k.format.KotlinxSerialization.auto

class GitHubClientSpec : WordSpec({
    "GitHub client" should {
        val httpClient = mockk<HttpHandler>()

        val organizationLens = Body.auto<GitHubOrganization>().toLens()

        "return null when getting an non existing organization" {
            val client = GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("orgs/fake-org")) } returns Response(Status.NOT_FOUND)

            val organisation = client.getOrganization("fake-org")

            organisation.shouldBeNull()
        }

        "return an organization when getting a existing organization" {
            val client = GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("orgs/github")) }
                .returns(Response(Status.OK).with(organizationLens of Defaults.organization))

            val organization = client.getOrganization("github")

            organization.shouldNotBeNull()

            organization.login shouldBe "github"
            organization.id shouldBe 1
            organization.name shouldBe "github"
            organization.company shouldBe "GitHub"
        }

        "return null when getting a team for a non-existing organization or repository" {
            val client = GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("repos/github/fake-repo")) } returns Response(Status.NOT_FOUND)

            val repository = client.getRepository("github", "fake-repo")

            repository.shouldBeNull()
        }

        "return an empty list when the organization has no repositories" {
            val client = GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("orgs/github/repos?page=1")) }
                .returns(Response(Status.OK).body(Json.encodeToString(emptyList<GitHubRepository>())))

            val repositories = client.getRepositories("github")

            repositories.shouldBeEmpty()
        }

        "return an empty list when the organization does not exist" {
            val client = GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("orgs/fake-org/repos?page=1")) }
                .returns(Response(Status.NOT_FOUND))

            val repositories = client.getRepositories("fake-org")

            repositories.shouldBeEmpty()
        }
    }
})