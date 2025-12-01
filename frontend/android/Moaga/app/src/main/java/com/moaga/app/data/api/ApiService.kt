package com.moaga.app.data.api

import com.moaga.app.data.api.dto.request.ChangeCategoryRequest
import com.moaga.app.data.api.dto.request.CurrentPlanRequest
import com.moaga.app.data.api.dto.request.DailyTransactionsRequest
import com.moaga.app.data.api.dto.request.EmailRequest
import com.moaga.app.data.api.dto.request.FinancialConnectRequest
import com.moaga.app.data.api.dto.request.FinancialDisconnectRequest
import com.moaga.app.data.api.dto.request.GroupCodeRequest
import com.moaga.app.data.api.dto.request.GroupCreateRequest
import com.moaga.app.data.api.dto.request.GroupInfoRequest
import com.moaga.app.data.api.dto.request.GroupJoinRequest
import com.moaga.app.data.api.dto.request.GroupMemberActionRequest
import com.moaga.app.data.api.dto.request.GroupOwnerRequest
import com.moaga.app.data.api.dto.request.InvitationInfoRequest
import com.moaga.app.data.api.dto.request.LoginRequest
import com.moaga.app.data.api.dto.request.MonthlyCalendarRequest
import com.moaga.app.data.api.dto.request.MyDailyTransactionsRequest
import com.moaga.app.data.api.dto.request.MyMonthlyCalendarRequest
import com.moaga.app.data.api.dto.request.SignUpRequest
import com.moaga.app.data.api.dto.request.SimpleLoginRequest
import com.moaga.app.data.api.dto.request.SmsSendRequest
import com.moaga.app.data.api.dto.request.SmsVerifyRequest
import com.moaga.app.data.api.dto.request.SpendingTrendRequest
import com.moaga.app.data.api.dto.request.TransactionIdRequest
import com.moaga.app.data.api.dto.request.report.GroupTotalExpenseRequest
import com.moaga.app.data.api.dto.request.report.ReportsListRequest
import com.moaga.app.data.api.dto.response.BaseResponse
import com.moaga.app.data.api.dto.response.ConnectedAccountsResponse
import com.moaga.app.data.api.dto.response.CurrentPlanResponse
import com.moaga.app.data.api.dto.response.DailyTransactionsResponse
import com.moaga.app.data.api.dto.response.GroupInfoResponse
import com.moaga.app.data.api.dto.response.GroupLastUpdatedResponse
import com.moaga.app.data.api.dto.response.GroupResponse
import com.moaga.app.data.api.dto.response.InvitationInfoResponse
import com.moaga.app.data.api.dto.response.LastUpdatedResponse
import com.moaga.app.data.api.dto.response.LinkableAccountsResponse
import com.moaga.app.data.api.dto.response.LoginResponse
import com.moaga.app.data.api.dto.response.MonthlyCalendarResponse
import com.moaga.app.data.api.dto.response.MyDailyTransactionsResponse
import com.moaga.app.data.api.dto.response.MyMonthlyCalendarResponse
import com.moaga.app.data.api.dto.response.MySpendingTrendResponse
import com.moaga.app.data.api.dto.response.SpendingTrendResponse
import com.moaga.app.data.api.dto.response.TodayExpenseResponse
import com.moaga.app.data.api.dto.response.UserInfoResponse
import com.moaga.app.data.api.dto.response.report.GroupTotalExpenseResponse
import com.moaga.app.data.api.dto.response.report.ReportListResponse
import com.moaga.app.data.utils.ApiConstants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// API 인터페이스 정의
interface ApiService {
    // Auth APIs - 실제 API는 LoginResponse를 직접 반환
    @POST(ApiConstants.AUTH_LOGIN)
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST(ApiConstants.AUTH_EMAIL_SEND)
    suspend fun sendEmailCode(@Body request: EmailRequest.SendEmailRequest): Response<Unit>

    @POST(ApiConstants.AUTH_EMAIL_VERIFY)
    suspend fun verifyEmailCode(@Body request: EmailRequest.VerifyEmailRequest): Response<Unit>

    @POST(ApiConstants.AUTH_SIGNUP)
    suspend fun signUp(@Body request: SignUpRequest): Response<Unit>

    @POST(ApiConstants.AUTH_LOGIN_SIMPLE)
    suspend fun loginWithSimple(@Body req: SimpleLoginRequest): LoginResponse

    @POST("v1/auth/simple-login")
    suspend fun simpleLogin(@Body request: SimpleLoginRequest): Response<LoginResponse>

    @POST("v1/users/me/info")
    suspend fun getUserInfo(): UserInfoResponse

    @POST("v1/auth/verify/sms/send")
    suspend fun sendSmsCode(@Body request: SmsSendRequest): Response<Unit>

    @POST("v1/auth/verify/sms/confirm")
    suspend fun verifySmsCode(@Body request: SmsVerifyRequest): Response<Unit>

    // Group APIs
    @POST("v1/groups/info")
    suspend fun getGroupInfo(@Body request: GroupInfoRequest): GroupInfoResponse

    @POST("v1/groups/info")
    suspend fun getGroupInfo(@Body request: Map<String, Int>): GroupInfoResponse

    @POST("v1/groups/invitation/info")
    suspend fun getInvitationInfo(@Body request: InvitationInfoRequest): InvitationInfoResponse

    @POST("v1/groups/invitation/info")
    suspend fun getGroupInfo(
        @Body request: GroupCodeRequest
    ): retrofit2.Response<GroupResponse>

    @POST("v1/groups/invitation/join")
    suspend fun joinGroup(@Body request: GroupJoinRequest): Response<Unit>

    @POST("v1/groups/owner")
    suspend fun checkGroupOwner(@Body request: GroupOwnerRequest): Boolean

    @POST("v1/groups/approve-member")
    suspend fun approveMember(@Body request: GroupMemberActionRequest): Boolean

    @POST("v1/groups/reject-member")
    suspend fun rejectMember(@Body request: GroupMemberActionRequest): Boolean

    @POST("/api/v1/groups/create")
    suspend fun createGroup(@Body request: GroupCreateRequest): Response<Unit>

    // Analysis APIs
    @POST("v1/analysis/group/today-expense")
    suspend fun getTodayExpense(): TodayExpenseResponse

    @POST("v1/analysis/group/last-updated")
    suspend fun getLastUpdated(): LastUpdatedResponse

    @POST("/api/v1/analysis/group/last-updated")
    suspend fun getGroupLastUpdated(): Response<GroupLastUpdatedResponse>

    @POST("v1/analysis/group/recent-trend")
    suspend fun getSpendingTrend(@Body request: SpendingTrendRequest): SpendingTrendResponse

    // Expense APIs
    @POST("v1/analysis/group/monthly-calendar")
    suspend fun getMonthlyCalendar(@Body request: MonthlyCalendarRequest): MonthlyCalendarResponse

    @POST("v1/analysis/group/daily-transactions")
    suspend fun getDailyTransactions(@Body request: DailyTransactionsRequest): DailyTransactionsResponse

    // Savings APIs
    @POST("v1/savings/plans/current")
    suspend fun getCurrentPlan(@Body request: CurrentPlanRequest): CurrentPlanResponse

    // Financial APIs
    @POST("v1/financial/list")
    suspend fun getLinkableAccounts(): LinkableAccountsResponse

    @POST("v1/financial/group/connect")
    suspend fun connectFinancial(@Body req: FinancialConnectRequest): Response<Unit>

    @POST("v1/financial/group/disconnect")
    suspend fun disconnectFinancial(@Body req: FinancialDisconnectRequest): Response<Unit>

    @POST("v1/financial/group/{groupId}/connected")
    suspend fun getConnectedAccounts(@Path("groupId") groupId: Long): ConnectedAccountsResponse

    // Transaction APIs
    @POST("v1/transactions/sync")
    suspend fun syncTransactions(): retrofit2.Response<Unit>

    @POST("v1/transactions/category/change")
    suspend fun changeTransactionCategory(@Body request: ChangeCategoryRequest): BaseResponse<Unit>

    @POST("v1/transactions/exclude")
    suspend fun excludeTransaction(@Body request: TransactionIdRequest): BaseResponse<Unit>

    @POST("v1/transactions/include")
    suspend fun includeTransaction(@Body request: TransactionIdRequest): BaseResponse<Unit>



    @POST("v1/analysis/group/total-expense")
    suspend fun getGroupTotalExpense(@Body request: GroupTotalExpenseRequest): GroupTotalExpenseResponse

    @POST("v1/reports/list")
    suspend fun getReportsList(@Body request: ReportsListRequest): List<ReportListResponse>

    // 개인 지출 APIs
    @POST("v1/transactions/my/monthly-calendar")
    suspend fun getMyMonthlyCalendar(@Body request: MyMonthlyCalendarRequest): MyMonthlyCalendarResponse

    @POST("v1/transactions/my/daily")
    suspend fun getMyDailyTransactions(@Body request: MyDailyTransactionsRequest): MyDailyTransactionsResponse

    @POST("v1/transactions/my/recent-trend")
    suspend fun getMySpendingTrend(): MySpendingTrendResponse
}
