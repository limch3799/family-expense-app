package com.moaga.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moaga.app.data.api.dto.response.LoginResponse
import com.moaga.app.data.api.dto.response.UserInfoResponse
import com.moaga.app.data.repository.AuthRepository
import com.moaga.app.data.utils.NetworkResult
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<NetworkResult<LoginResponse>?>()
    val loginResult: LiveData<NetworkResult<LoginResponse>?> = _loginResult

    private val _simpleLoginResult = MutableLiveData<NetworkResult<LoginResponse>?>()
    val simpleLoginResult: LiveData<NetworkResult<LoginResponse>?> = _simpleLoginResult

    private val _userInfoResult = MutableLiveData<NetworkResult<UserInfoResponse>?>()
    val userInfoResult: LiveData<NetworkResult<UserInfoResponse>?> = _userInfoResult

    private val _groupInfoResult = MutableLiveData<NetworkResult<String>?>()
    val groupInfoResult: LiveData<NetworkResult<String>?> = _groupInfoResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _loginResult.value = NetworkResult.Error("이메일과 비밀번호를 입력해주세요")
                return@launch
            }

            _loginResult.value = NetworkResult.Loading()
            val result = repository.login(email, password)
            _loginResult.value = result
        }
    }

    fun simpleLogin(email: String, simplePassword: String) {
        viewModelScope.launch {
            if (email.isBlank() || simplePassword.isBlank()) {
                _simpleLoginResult.value = NetworkResult.Error("이메일과 간편비밀번호를 입력해주세요")
                return@launch
            }

            if (simplePassword.length != 6) {
                _simpleLoginResult.value = NetworkResult.Error("간편비밀번호는 6자리여야 합니다")
                return@launch
            }

            _simpleLoginResult.value = NetworkResult.Loading()
            val result = repository.simpleLogin(email, simplePassword)
            _simpleLoginResult.value = result
        }
    }

    // 사용자 정보 수동 조회
    fun fetchUserInfo() {
        viewModelScope.launch {
            _userInfoResult.value = NetworkResult.Loading()
            val result = repository.fetchAndSaveUserInfo()
            _userInfoResult.value = result
        }
    }

    // 그룹 정보 수동 조회
    fun fetchGroupInfo() {
        viewModelScope.launch {
            _groupInfoResult.value = NetworkResult.Loading()
            val result = repository.fetchAndSaveGroupInfo()
            _groupInfoResult.value = result
        }
    }

    // 사용자 정보 getter 메서드들 (로컬 저장소에서 즉시 조회)
    fun getCurrentUserId(): Int = repository.getCurrentUserId()
    fun getCurrentGroupId(): Int = repository.getCurrentGroupId()
    fun getCurrentUsername(): String? = repository.getCurrentUsername()
    fun getCurrentEmail(): String? = repository.getCurrentEmail()
    fun getCurrentAccessToken(): String? = repository.getCurrentAccessToken()

    // 그룹 정보 getter 메서드들 (로컬 저장소에서 즉시 조회)
    fun getGroupName(): String? = repository.getGroupName()
    fun getGroupDescription(): String? = repository.getGroupDescription()
    fun getGroupOwnerName(): String? = repository.getGroupOwnerName()
    fun getGroupMemberCount(): Int = repository.getGroupMemberCount()
    fun getGroupInviteCode(): String? = repository.getGroupInviteCode()
    fun isGroupOwner(): Boolean = repository.isGroupOwner()

    // 상태 확인 메서드들
    fun isLoggedIn(): Boolean = repository.isLoggedIn()
    fun hasUserInfo(): Boolean = repository.hasUserInfo()
    fun hasGroupInfo(): Boolean = repository.hasGroupInfo()

    // 로그아웃
    fun logout() {
        repository.logout()
    }

    // 결과 클리어 메서드들
    fun clearLoginResult() {
        _loginResult.value = null
    }

    fun clearSimpleLoginResult() {
        _simpleLoginResult.value = null
    }

    fun clearUserInfoResult() {
        _userInfoResult.value = null
    }

    fun clearGroupInfoResult() {
        _groupInfoResult.value = null
    }
}