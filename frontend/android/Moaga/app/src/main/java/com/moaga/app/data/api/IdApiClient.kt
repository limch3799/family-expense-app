// file: app/src/main/java/com/moaga/app/data/api/IdApiClient.kt
package com.moaga.app.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object IdApiClient {
    val api: IdApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://80e88619783f.ngrok-free.app/") // 반드시 / 로 끝나야 함
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IdApiService::class.java)
    }
}
