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

All service methods are `suspend` functions. Call them from a coroutine or use `runBlocking` for one-off use.

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

`GitHubClient` exposes three service objects:
- `client.organizations` — `OrganizationService`
- `client.repositories` — `RepositoryService`
- `client.teams` — `TeamService`

---

### ApiResult

Single-resource lookups return `ApiResult<T>`, a sealed type:

```kotlin
sealed class ApiResult<out T> {
    data class Found<out T>(val value: T) : ApiResult<T>()  // resource exists
    data object NotFound : ApiResult<Nothing>()             // HTTP 404
    data class Failure(val exception: GitHubApiException)   // unexpected error
}
```

Use `when` to handle all cases at compile time, or convenience helpers:

```kotlin
val result = client.organizations.get("my-org")

when (result) {
    is ApiResult.Found   -> println(result.value.login)
    is ApiResult.NotFound -> println("not found")
    is ApiResult.Failure  -> println("error: ${result.exception.message}")
}

// Or simply throw on failure / not-found:
val org = client.organizations.get("my-org").getOrThrow()

// Or get null on non-success:
val org = client.organizations.get("my-org").getOrNull()
```

List operations return `ApiResult<List<T>>` — `NotFound` is never returned for lists.

---

### Organizations

```kotlin
// Fetch a single organization
val org = client.organizations.get("my-org").getOrThrow()

// List members
val members = client.organizations.getMembers(org).getOrThrow()

// List repositories
val repos = client.organizations.getRepositories(org).getOrThrow()

// Invite a user by GitHub user ID
val invite = client.organizations.invite(org, inviteeId = 1234567)

// List teams
val teams = client.organizations.getTeams(org).getOrThrow()

// Get a single team
val team = client.organizations.getTeam(org, "platform").getOrThrow()

// Create a team
val newTeam = client.organizations.createTeam(
    organizationName = org.login,
    teamName = "platform",
    description = "Platform engineering",
    privacy = TeamPrivacy.Secret
).getOrThrow()
```

---

### Repositories

```kotlin
// Fetch a single repository
val repo = client.repositories.get("my-org", "my-repo").getOrThrow()

// List teams with explicit access
val teams = client.repositories.getTeams(repo).getOrThrow()

// List collaborators
val all     = client.repositories.getAllCollaborators(repo).getOrThrow()
val direct  = client.repositories.getDirectCollaborators(repo).getOrThrow()
val outside = client.repositories.getOutsideCollaborators(repo).getOrThrow()

// Transfer to another owner
client.repositories.transfer(repo, newOwner = "new-org")
```

---

### Teams

```kotlin
val team = client.organizations.getTeam("my-org", "platform").getOrThrow()

// Members
val members = client.teams.getMembers(team).getOrThrow()
client.teams.addMember(team, "octocat")
client.teams.removeMember(team, "octocat")

// Repositories
val repos = client.teams.getRepositories(team).getOrThrow()
client.teams.addRepository(team, "my-repo", Permission.Push)
```

---

### Error handling

`GitHubApiException` is only thrown if something unexpected happens outside the service layer (e.g., a network-level error). The service methods themselves return `ApiResult` and never throw:

```kotlin
val result = client.organizations.get("my-org")
if (result is ApiResult.Failure) {
    println("GitHub API error: ${result.exception.status.code} — ${result.exception.message}")
}
```

---

## Logging

The library uses [SLF4J](https://www.slf4j.org/) for logging. Add your preferred SLF4J backend (e.g. Logback) to your project and set the log level for `nl.ordina.github` to `DEBUG` to see request/pagination details.

## License

Apache 2.0 — see [LICENSE](LICENSE).
