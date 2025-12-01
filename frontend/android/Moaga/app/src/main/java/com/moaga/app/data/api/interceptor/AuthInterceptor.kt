package com.moaga.app.data.api.interceptor

import com.moaga.app.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 로그인/회원가입은 토큰 없이 요청
        val path = originalRequest.url.encodedPath
        if (path.contains("auth/login") || path.contains("auth/register")) {
            return chain.proceed(originalRequest)
        }

        // 저장된 액세스 토큰 가져오기
        val accessToken = tokenManager.getAccessToken()

        val newRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(newRequest)

        // 토큰 만료 등으로 401 응답이 오면 -> 토큰 삭제 + 로그인 상태 해제
        if (response.code == 401) {
            response.close()
            tokenManager.clearToken()
            // 여기서 바로 앱 전체 로그아웃 로직 트리거 가능 (예: 이벤트 버스 or 콜백)
        }

        return response
    }
}
