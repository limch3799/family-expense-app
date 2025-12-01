package com.moaga.app.data.repository

import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.ApiService
import com.moaga.app.data.api.dto.request.report.GroupTotalExpenseRequest
import com.moaga.app.data.api.dto.request.report.ReportsListRequest
import com.moaga.app.data.api.dto.response.report.GroupTotalExpenseResponse
import com.moaga.app.data.api.dto.response.report.ReportListResponse


class ReportRepository {
    private val apiService: ApiService = ApiClient.apiService

    suspend fun getGroupTotalExpense(request: GroupTotalExpenseRequest): GroupTotalExpenseResponse {
        return apiService.getGroupTotalExpense(request)
    }

    suspend fun getReportsList(request: ReportsListRequest): List<ReportListResponse> {
        return apiService.getReportsList(request)
    }
}