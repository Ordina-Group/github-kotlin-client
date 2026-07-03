package com.soprasteria.github

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Status

class ApiResultSpec :
    WordSpec({

        val failure = GitHubApiException(Status.INTERNAL_SERVER_ERROR, "boom")

        "ApiResult.map" should {

            "transform Found values" {
                ApiResult.Found(21).map { it * 2 } shouldBe ApiResult.Found(42)
            }

            "pass through NotFound unchanged" {
                ApiResult.NotFound.map { value: Int -> value * 2 } shouldBe ApiResult.NotFound
            }

            "pass through Failure unchanged" {
                ApiResult.Failure(failure).map { value: Int -> value * 2 } shouldBe
                    ApiResult.Failure(failure)
            }
        }

        "ApiResult.flatMap" should {

            "transform Found values" {
                ApiResult.Found("main").flatMap { ApiResult.Found(it.uppercase()) } shouldBe
                    ApiResult.Found("MAIN")
            }

            "pass through NotFound unchanged" {
                ApiResult.NotFound.flatMap { value: String -> ApiResult.Found(value.uppercase()) } shouldBe
                    ApiResult.NotFound
            }

            "pass through Failure unchanged" {
                ApiResult.Failure(failure).flatMap { value: String ->
                    ApiResult.Found(value.uppercase())
                } shouldBe ApiResult.Failure(failure)
            }
        }
    })