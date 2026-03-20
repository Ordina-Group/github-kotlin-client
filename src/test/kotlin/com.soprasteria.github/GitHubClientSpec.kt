package com.soprasteria.github

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.soprasteria.github.organization.GitHubOrganization
import com.soprasteria.github.repository.GitHubRepository
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

class GitHubClientSpec : WordSpec({

    val httpClient = mockk<HttpHandler>()
    val client = GitHubClient(httpClient)

    val organizationLens = Body.auto<GitHubOrganization>().toLens()

    "organizations.get" should {

        "return NotFound when the organization does not exist" {
            every { httpClient.invoke(matchUri("/orgs/fake-org")) } returns Response(Status.NOT_FOUND)

            client.organizations.get("fake-org") shouldBe ApiResult.NotFound
        }

        "return Found with the organization when it exists" {
            every { httpClient.invoke(matchUri("/orgs/github")) }
                .returns(Response(Status.OK).with(organizationLens of Defaults.organization()))

            val result = client.organizations.get("github")

            result.shouldBeInstanceOf<ApiResult.Found<GitHubOrganization>>()
            val org = result.getOrThrow()
            org.login shouldBe "github"
            org.id shouldBe 1
            org.name shouldBe "github"
            org.company shouldBe "GitHub"
        }

        "return Failure on server error" {
            every { httpClient.invoke(matchUri("/orgs/github")) } returns Response(Status.INTERNAL_SERVER_ERROR)

            val result = client.organizations.get("github")

            result.shouldBeInstanceOf<ApiResult.Failure>()
            (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
        }
    }

    "repositories.get" should {

        "return NotFound when the repository does not exist" {
            every { httpClient.invoke(matchUri("/repos/github/fake-repo")) } returns Response(Status.NOT_FOUND)

            client.repositories.get("github", "fake-repo") shouldBe ApiResult.NotFound
        }

        "return Failure on server error" {
            every { httpClient.invoke(matchUri("/repos/github/Mona-Liza")) } returns Response(Status.INTERNAL_SERVER_ERROR)

            val result = client.repositories.get("github", "Mona-Liza")

            result.shouldBeInstanceOf<ApiResult.Failure>()
            (result as ApiResult.Failure).exception.status shouldBe Status.INTERNAL_SERVER_ERROR
        }
    }

    "organizations.getRepositories" should {

        "return Found with empty list when the organization has no repositories" {
            every { httpClient.invoke(matchUri("/orgs/github/repos?page=1&per_page=100")) }
                .returns(Response(Status.OK).body(Json.encodeToString(emptyList<GitHubRepository>())))

            val result = client.organizations.getRepositories("github")

            result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepository>>>()
            result.getOrThrow() shouldBe emptyList()
        }

        "return Failure when the organization does not exist" {
            every { httpClient.invoke(matchUri("/orgs/fake-org/repos?page=1&per_page=100")) }
                .returns(Response(Status.NOT_FOUND))

            client.organizations.getRepositories("fake-org").shouldBeInstanceOf<ApiResult.Failure>()
        }
    }

    "organizations.getRepositories pagination" should {

        "follow Link rel=next headers across multiple pages" {
            val repo = Defaults.repository()
            val linkHeader = """<https://api.github.com/orgs/github/repos?page=2>; rel="next""""

            every { httpClient.invoke(matchUri("/orgs/github/repos?page=1&per_page=100")) }
                .returns(
                    Response(Status.OK)
                        .header("Link", linkHeader)
                        .body(Json.encodeToString(listOf(repo)))
                )
            every { httpClient.invoke(matchUri("/orgs/github/repos?page=2&per_page=100")) }
                .returns(Response(Status.OK).body(Json.encodeToString(listOf(repo))))

            val result = client.organizations.getRepositories("github")

            result.shouldBeInstanceOf<ApiResult.Found<List<GitHubRepository>>>()
            result.getOrThrow().size shouldBe 2
        }
    }
})