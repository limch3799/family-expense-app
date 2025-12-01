package com.moaga.app.data.repository

import com.moaga.app.data.api.ApiClient
import com.moaga.app.data.api.dto.request.*
import com.moaga.app.data.api.dto.response.*
import com.moaga.app.data.utils.NetworkResult
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {
    private val apiService = ApiClient.apiService
    private val tokenManager = ApiClient.getTokenManager()

    suspend fun login(email: String, password: String): NetworkResult<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            // 로그인 성공시 토큰과 userId 저장
            tokenManager.saveTokens(response.accessToken, response.userId)

            // 로그인 후 자동으로 사용자 정보 조회 및 저장
            fetchAndSaveUserInfo()

            // 사용자 정보 조회 후 그룹 정보도 자동 조회
            fetchAndSaveGroupInfo()

            NetworkResult.Success(response)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> NetworkResult.Error("이메일 또는 비밀번호가 잘못되었습니다.", e.code())
                else -> NetworkResult.Error("이메일 또는 비밀번호가 잘못되었습니다.", e.code())
            }
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }
    suspend fun simpleLogin(email: String, simplePassword: String): NetworkResult<LoginResponse> {
        return try {
            val request = SimpleLoginRequest(email, simplePassword)
            val response = apiService.simpleLogin(request)

            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // 토큰 저장
                    tokenManager.saveTokens(loginResponse.accessToken, loginResponse.userId)

                    // 사용자 정보 및 그룹 정보 자동 로드
                    fetchAndSaveUserInfo()
                    fetchAndSaveGroupInfo()

                    NetworkResult.Success(loginResponse)
                } ?: NetworkResult.Error("응답 데이터가 없습니다")
            } else {
                NetworkResult.Error("간편 로그인에 실패했습니다")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "네트워크 오류가 발생했습니다")
        }
    }


    // 사용자 정보 조회 및 저장
    suspend fun fetchAndSaveUserInfo(): NetworkResult<UserInfoResponse> {
        return try {
            val response = apiService.getUserInfo()

            // 사용자 정보 저장
            tokenManager.saveUserInfo(
                userId = response.userId,
                groupId = response.groupId,
                email = response.email,
                username = response.username,
                phoneNumber = response.phoneNumber,
                birthDate = response.birthDate,
                gender = response.gender
            )

            NetworkResult.Success(response)
        } catch (e: HttpException) {
            NetworkResult.Error("사용자 정보를 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    // 그룹 정보 조회 및 저장 (3개 API 통합)
    suspend fun fetchAndSaveGroupInfo(): NetworkResult<String> {
        return try {
            val groupId = tokenManager.getGroupId()
            val response = apiService.getGroupInfo(GroupInfoRequest(groupId))
            if (groupId == -1) {
                return NetworkResult.Error("그룹 ID를 찾을 수 없습니다.")
            }

            // 1. 그룹 기본 정보 조회
            val groupInfoResponse = apiService.getGroupInfo(GroupInfoRequest(groupId))

            // 2. 초대 코드로 상세 정보 조회
            val invitationResponse = apiService.getInvitationInfo(
                InvitationInfoRequest(groupInfoResponse.inviteCode)
            )

            // 3. 그룹장 여부 확인
            val isOwner = apiService.checkGroupOwner(GroupOwnerRequest(groupId))

            // 통합된 그룹 정보 저장
            tokenManager.saveGroupInfo(
                savingAccountNo = groupInfoResponse.savingAccountNo,
                inviteCode = groupInfoResponse.inviteCode,
                amount = groupInfoResponse.amount,
                groupName = invitationResponse.name,
                groupDescription = invitationResponse.description,
                ownerName = invitationResponse.ownerName,
                memberCount = invitationResponse.memberCount,
                isGroupOwner = isOwner,
                planId = groupInfoResponse.planId

            )

            tokenManager.saveGroupMembers(response.joinedMembers)

            NetworkResult.Success("그룹 정보 저장 완료")
        } catch (e: HttpException) {
            NetworkResult.Error("그룹 정보를 가져올 수 없습니다.", e.code())
        } catch (e: IOException) {
            NetworkResult.Error("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    // 사용자 정보 getter 메서드들
    fun getCurrentUserId(): Int = tokenManager.getUserId()
    fun getCurrentGroupId(): Int = tokenManager.getGroupId()
    fun getCurrentUsername(): String? = tokenManager.getUsername()
    fun getCurrentEmail(): String? = tokenManager.getEmail()
    fun getCurrentAccessToken(): String? = tokenManager.getAccessToken()

    // 그룹 정보 getter 메서드들
    fun getGroupName(): String? = tokenManager.getGroupName()
    fun getGroupDescription(): String? = tokenManager.getGroupDescription()
    fun getGroupOwnerName(): String? = tokenManager.getGroupOwnerName()
    fun getGroupMemberCount(): Int = tokenManager.getGroupMemberCount()
    fun getGroupInviteCode(): String? = tokenManager.getGroupInviteCode()
    fun isGroupOwner(): Boolean = tokenManager.isGroupOwner()

    // planId getter 메서드 추가
    fun getCurrentPlanId(): Int = tokenManager.getPlanId()
    fun hasPlan(): Boolean = tokenManager.hasPlan()


    // 상태 확인
    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    fun hasUserInfo(): Boolean = tokenManager.hasUserInfo()
    fun hasGroupInfo(): Boolean = tokenManager.hasGroupInfo()

    // 로그아웃
    fun logout() {
        tokenManager.clearToken()
    }
}