package com.example.yasuda.weathers.data

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Failure(val statusCode: Int?, val message: String?) : NetworkResult<Nothing>()
}