package com.moaga.app.data.repository

import com.moaga.app.data.api.ApiService
import com.moaga.app.data.api.dto.request.CurrentPlanRequest
import com.moaga.app.data.api.dto.response.CurrentPlanResponse

class PlanRepository (
    private val apiService: ApiService
) {
    suspend fun getCurrentPlan(planId: Long): CurrentPlanResponse {
        return apiService.getCurrentPlan(CurrentPlanRequest(planId))
    }
}