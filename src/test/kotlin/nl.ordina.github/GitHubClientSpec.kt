package nl.ordina.github

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.repository.GitHubRepository
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

class GitHubClientSpec : WordSpec({
    "GitHub client" should {
        val httpClient = mockk<HttpHandler>()
        val client = GitHubClient(httpClient)

        val organizationLens = Body.auto<GitHubOrganization>().toLens()

        "return null when getting a non-existing organization" {
            every { httpClient.invoke(matchUri("orgs/fake-org")) } returns Response(Status.NOT_FOUND)

            client.getOrganization("fake-org").shouldBeNull()
        }

        "return an organization when getting an existing organization" {
            every { httpClient.invoke(matchUri("orgs/github")) }
                .returns(Response(Status.OK).with(organizationLens of Defaults.organization(httpClient)))

            val organization = client.getOrganization("github")

            organization.shouldNotBeNull()
            organization.login shouldBe "github"
            organization.id shouldBe 1
            organization.name shouldBe "github"
            organization.company shouldBe "GitHub"
        }

        "return null when getting a repository that does not exist" {
            every { httpClient.invoke(matchUri("repos/github/fake-repo")) } returns Response(Status.NOT_FOUND)

            client.getRepository("github", "fake-repo").shouldBeNull()
        }

        "return an empty list when the organization has no repositories" {
            every { httpClient.invoke(matchUri("orgs/github/repos?page=1&per_page=100")) }
                .returns(Response(Status.OK).body(Json.encodeToString(emptyList<GitHubRepository>())))

            client.getRepositories("github").shouldBeEmpty()
        }

        "throw GitHubApiException when getting repositories for a non-existent organization" {
            every { httpClient.invoke(matchUri("orgs/fake-org/repos?page=1&per_page=100")) }
                .returns(Response(Status.NOT_FOUND))

            shouldThrow<GitHubApiException> { client.getRepositories("fake-org") }
        }
    }

    "GitHub client pagination" should {
        "follow Link rel=next headers across multiple pages" {
            val httpClient = mockk<HttpHandler>()
            val client = GitHubClient(httpClient)
            val repo = Defaults.repository(httpClient)

            val linkHeader = """<https://api.github.com/orgs/github/repos?page=2>; rel="next""""

            every { httpClient.invoke(matchUri("orgs/github/repos?page=1&per_page=100")) }
                .returns(
                    Response(Status.OK)
                        .header("Link", linkHeader)
                        .body(Json.encodeToString(listOf(repo)))
                )
            every { httpClient.invoke(matchUri("orgs/github/repos?page=2&per_page=100")) }
                .returns(Response(Status.OK).body(Json.encodeToString(listOf(repo))))

            client.getRepositories("github") shouldHaveSize 2
        }
    }

    "GitHub client API errors" should {
        "throw GitHubApiException when the API returns a server error for getOrganization" {
            val httpClient = mockk<HttpHandler>()
            val client = GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("orgs/github")) }
                .returns(Response(Status.INTERNAL_SERVER_ERROR))

            val exception = shouldThrow<GitHubApiException> { client.getOrganization("github") }
            exception.status shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "throw GitHubApiException when the API returns a server error for getRepository" {
            val httpClient = mockk<HttpHandler>()
            val client = GitHubClient(httpClient)

            every { httpClient.invoke(matchUri("repos/github/Mona-Liza")) }
                .returns(Response(Status.INTERNAL_SERVER_ERROR))

            val exception = shouldThrow<GitHubApiException> { client.getRepository("github", "Mona-Liza") }
            exception.status shouldBe Status.INTERNAL_SERVER_ERROR
        }
    }
})