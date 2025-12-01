package com.moaga.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moaga.app.data.api.dto.response.GroupMember

class TokenManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "moaga_prefs"
        // 사용자 정보
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val USER_ID_KEY = "user_id"
        private const val GROUP_ID_KEY = "group_id"
        private const val USERNAME_KEY = "username"
        private const val EMAIL_KEY = "email"
        private const val PHONE_NUMBER_KEY = "phone_number"
        private const val BIRTH_DATE_KEY = "birth_date"
        private const val GENDER_KEY = "gender"
        private const val LOGIN_TIME_KEY = "login_time"

        // 그룹 정보
        private const val GROUP_SAVING_ACCOUNT_NO_KEY = "group_saving_account_no"
        private const val GROUP_INVITE_CODE_KEY = "group_invite_code"
        private const val GROUP_AMOUNT_KEY = "group_amount"
        private const val GROUP_NAME_KEY = "group_name"
        private const val GROUP_DESCRIPTION_KEY = "group_description"
        private const val GROUP_OWNER_NAME_KEY = "group_owner_name"
        private const val GROUP_MEMBER_COUNT_KEY = "group_member_count"
        private const val IS_GROUP_OWNER_KEY = "is_group_owner"
        private const val GROUP_MEMBERS_KEY = "group_members"

        private const val KEY_PLAN_ID = "plan_id"

        // 간편 로그인
        private const val QUICK_ENABLED_KEY = "quick_enabled"
        private const val QUICK_EMAIL_KEY = "quick_email"
    }

    // 토큰 및 사용자 기본 정보
    fun saveTokens(accessToken: String, userId: Int) {
        prefs.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putInt(USER_ID_KEY, userId)
            .putLong(LOGIN_TIME_KEY, System.currentTimeMillis())
            .apply()
    }

    fun saveUserInfo(
        userId: Int,
        groupId: Int,
        email: String,
        username: String,
        phoneNumber: String,
        birthDate: String,
        gender: String
    ) {
        prefs.edit()
            .putInt(USER_ID_KEY, userId)
            .putInt(GROUP_ID_KEY, groupId)
            .putString(EMAIL_KEY, email)
            .putString(USERNAME_KEY, username)
            .putString(PHONE_NUMBER_KEY, phoneNumber)
            .putString(BIRTH_DATE_KEY, birthDate)
            .putString(GENDER_KEY, gender)
            .apply()
    }

    // 그룹 정보 저장
    fun saveGroupInfo(
        savingAccountNo: String,
        inviteCode: String,
        amount: Int,
        groupName: String,
        groupDescription: String,
        ownerName: String,
        memberCount: Int,
        isGroupOwner: Boolean,
        planId: Int? = null
    ) {
        prefs.edit()
            .putString(GROUP_SAVING_ACCOUNT_NO_KEY, savingAccountNo)
            .putString(GROUP_INVITE_CODE_KEY, inviteCode)
            .putInt(GROUP_AMOUNT_KEY, amount)
            .putString(GROUP_NAME_KEY, groupName)
            .putString(GROUP_DESCRIPTION_KEY, groupDescription)
            .putString(GROUP_OWNER_NAME_KEY, ownerName)
            .putInt(GROUP_MEMBER_COUNT_KEY, memberCount)
            .putBoolean(IS_GROUP_OWNER_KEY, isGroupOwner)
            .putInt(KEY_PLAN_ID, planId ?: -1)
            .apply()
    }

    fun saveGroupMembers(members: List<GroupMember>) {
        val json = Gson().toJson(members)
        prefs.edit().putString(GROUP_MEMBERS_KEY, json).apply()
    }

    fun getGroupMembers(): List<GroupMember> {
        val json = prefs.getString(GROUP_MEMBERS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<GroupMember>>() {}.type
            Gson().fromJson(json, type)
        } else emptyList()
    }

    // 기본 정보 getter 메서드들
    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN_KEY, null)
    fun getUserId(): Int = prefs.getInt(USER_ID_KEY, -1)
    fun getGroupId(): Int = prefs.getInt(GROUP_ID_KEY, -1)
    fun getUsername(): String? = prefs.getString(USERNAME_KEY, null)
    fun getEmail(): String? = prefs.getString(EMAIL_KEY, null)
    fun getPhoneNumber(): String? = prefs.getString(PHONE_NUMBER_KEY, null)
    fun getBirthDate(): String? = prefs.getString(BIRTH_DATE_KEY, null)
    fun getGender(): String? = prefs.getString(GENDER_KEY, null)

    // 그룹 정보 getter 메서드들
    fun getGroupSavingAccountNo(): String? = prefs.getString(GROUP_SAVING_ACCOUNT_NO_KEY, null)
    fun getGroupInviteCode(): String? = prefs.getString(GROUP_INVITE_CODE_KEY, null)
    fun getGroupAmount(): Int = prefs.getInt(GROUP_AMOUNT_KEY, 0)
    fun getGroupName(): String? = prefs.getString(GROUP_NAME_KEY, null)
    fun getGroupDescription(): String? = prefs.getString(GROUP_DESCRIPTION_KEY, null)
    fun getGroupOwnerName(): String? = prefs.getString(GROUP_OWNER_NAME_KEY, null)
    fun getGroupMemberCount(): Int = prefs.getInt(GROUP_MEMBER_COUNT_KEY, 0)
    fun isGroupOwner(): Boolean = prefs.getBoolean(IS_GROUP_OWNER_KEY, false)

    // planId 조회 메서드 추가
    fun getPlanId(): Int {
        return prefs.getInt(KEY_PLAN_ID, -1)
    }

    // planId가 있는지 확인하는 메서드
    fun hasPlan(): Boolean {
        return getPlanId() != -1
    }

    // 간편 로그인 관련 메서드들
    fun setQuickLoginEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(QUICK_ENABLED_KEY, enabled).apply()
    }

    fun isQuickLoginEnabled(): Boolean = prefs.getBoolean(QUICK_ENABLED_KEY, false)

    fun setQuickLoginEmail(email: String) {
        prefs.edit().putString(QUICK_EMAIL_KEY, email).apply()
    }

    fun getQuickLoginEmail(): String? = prefs.getString(QUICK_EMAIL_KEY, null)

    fun clearQuickLogin() {
        prefs.edit()
            .remove(QUICK_ENABLED_KEY)
            .remove(QUICK_EMAIL_KEY)
            .apply()
    }

    // 상태 확인 메서드들
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getUserId() != -1
    }

    fun hasUserInfo(): Boolean {
        return getUsername() != null && getGroupId() != -1
    }

    fun hasGroupInfo(): Boolean {
        return getGroupName() != null && getGroupInviteCode() != null
    }

    // 토큰 관리
    fun updateAccessToken(newToken: String) {
        prefs.edit()
            .putString(ACCESS_TOKEN_KEY, newToken)
            .putLong(LOGIN_TIME_KEY, System.currentTimeMillis())
            .apply()
    }

    // 데이터 삭제
    fun clearToken() {
        prefs.edit().clear().apply()
    }
}