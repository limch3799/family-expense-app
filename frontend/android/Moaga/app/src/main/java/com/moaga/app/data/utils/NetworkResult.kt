package com.moaga.app.data.utils

// 네트워크 요청 결과를 하나로 표현
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int = -1) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()
}

// NetworkResult 확장 함수들
fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

fun <T> NetworkResult<T>.onError(action: (String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(message)
    return this
}

fun <T> NetworkResult<T>.onLoading(action: (Boolean) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) action(isLoading)
    return this
}