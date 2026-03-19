package nl.ordina.github

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.mockk.every
import io.mockk.mockk
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

class GitHubRepositorySpec : WordSpec({

    "A GitHub repository" should {

        "be able to get a list of teams with explicit access to the repository" {
            val httpClient = mockk<HttpHandler>()
            val repository = Defaults.repository(httpClient)

            every { httpClient.invoke(matchUri("/repos/${Defaults.owner}/${repository.name}/teams")) } returns
                Response(Status.OK).body("[]")

            repository.getTeams().shouldBeEmpty()
        }
    }
})