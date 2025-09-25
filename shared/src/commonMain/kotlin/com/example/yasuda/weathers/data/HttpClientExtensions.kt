package com.example.yasuda.weathers.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.isSuccess

suspend inline fun <reified T> HttpClient.safeRequest(
    block: HttpRequestBuilder.() -> Unit
): NetworkResult<T> {
    return try {
        val response = this.get { block() }
        if (response.status.isSuccess()) {
            val body = response.body<T>()
            NetworkResult.Success(body)
        } else {
            NetworkResult.Failure(response.status.value, response.body<String>())
        }
    } catch (e: Exception) {
        NetworkResult.Failure(null, e.message?: "An unknown error occurred")
    }
}