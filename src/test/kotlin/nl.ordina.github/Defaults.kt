package nl.ordina.github

import nl.ordina.github.organization.GitHubOrganization
import nl.ordina.github.repository.GitHubRepository

object Defaults {
    val organization = GitHubOrganization(
        login = "github",
        id = 1,
        node_id = "MDEyOk9yZ2FuaXphdGlvbjE=",
        name = "github",
        company = "GitHub"
    )

    val repository = GitHubRepository(
        owner = "github",
        id = 1,
        name = "Mona-Liza",
        full_name = "github/Mona-Liza"
    )
}