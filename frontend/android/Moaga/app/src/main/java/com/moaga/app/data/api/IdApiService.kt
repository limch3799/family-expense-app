// file: app/src/main/java/com/moaga/app/data/api/IdApiService.kt
package com.moaga.app.data.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import retrofit2.http.Body
import retrofit2.http.POST

// ✅ Request
data class IdRequest(
    val encrypted_image: String,
    val password: String? = null
)

// ✅ Response
@Parcelize
data class IdResponse(
    val success: Boolean,
    val need_retake: Boolean,
    val data: IdData?,
    val ocr_confidence: Double?,
    val ocr_status: String?,
    val message: String?
) : Parcelable

@Parcelize
data class IdData(
    val name: String,
    val id_number_front: String,
    val id_number_back_first: String,
    val issue_date: String
) : Parcelable

interface IdApiService {
    @POST("/api/step1-ocr")
    suspend fun sendIdCard(@Body request: IdRequest): IdResponse
}
