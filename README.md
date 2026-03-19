# github-kotlin-client

A Kotlin library for interacting with the [GitHub REST API](https://docs.github.com/en/rest), built on [http4k](https://www.http4k.org/) and [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization).

## Requirements

- JVM 21+
- Kotlin 2.x

## Installation

The library is published to GitHub Packages.

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Ordina-Group/github-kotlin-client")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("nl.ordina:github-kotlin-client:<version>")
}
```

## Usage

### Creating a client

```kotlin
val client = GitHubClient.create(token = System.getenv("GITHUB_TOKEN"))
```

To target a GitHub Enterprise Server instance, pass the base URL:

```kotlin
val client = GitHubClient.create(
    token = System.getenv("GITHUB_TOKEN"),
    baseUrl = "https://github.example.com/api/v3"
)
```

---

### Organizations

```kotlin
// Fetch an organization (returns null if not found)
val org = client.getOrganization("my-org") ?: error("org not found")

// List members
val members: List<GitHubOrganizationMember> = org.getMembers()

// List repositories
val repos: List<GitHubRepository> = org.getRepositories()

// Invite a user by GitHub user ID
val invite: GitHubOrganizationInvite? = org.invite(inviteeId = 1234567)
```

---

### Repositories

```kotlin
// Fetch a single repository (returns null if not found)
val repo = client.getRepository("my-org", "my-repo") ?: error("repo not found")

// Fetch all repositories for an organization
val repos = client.getRepositories("my-org")

// List teams with explicit access
val teams: List<GitHubRepositoryTeam> = repo.getTeams()

// List collaborators
val all      = repo.getAllCollaborators()
val direct   = repo.getDirectCollaborators()
val outside  = repo.getOutsideCollaborators()

// Transfer to another owner
repo.transfer(newOwner = "new-org")
```

---

### Teams

```kotlin
val org = client.getOrganization("my-org") ?: error("org not found")

// List all teams
val teams: List<GitHubTeam> = org.getTeams()

// Fetch a single team by slug
val team = org.getTeam("my-team") ?: error("team not found")

// Create a team
val newTeam = org.createTeam(
    teamName = "platform",
    teamDescription = "Platform engineering",
    privacy = TeamPrivacy.Secret
)

// Team members
val members: List<GitHubTeamMember> = team.getMembers()
team.addMember("octocat")
team.removeMember("octocat")

// Team repositories
val teamRepos: List<GitHubTeamRepository> = team.getRepositories()
team.addRepository("my-repo", Permission.Push)
```

---

### Error handling

All unexpected HTTP responses (5xx, etc.) throw a `GitHubApiException`:

```kotlin
try {
    val org = client.getOrganization("my-org")
} catch (e: GitHubApiException) {
    println("GitHub API error: ${e.status.code} — ${e.message}")
}
```

`null` is returned only for expected "not found" cases (HTTP 404).

---

## Logging

The library uses [SLF4J](https://www.slf4j.org/) for logging. Add your preferred SLF4J backend (e.g. Logback) to your project and set the log level for `nl.ordina.github` to `DEBUG` to see request/pagination details.

## License

Apache 2.0 — see [LICENSE](LICENSE).
