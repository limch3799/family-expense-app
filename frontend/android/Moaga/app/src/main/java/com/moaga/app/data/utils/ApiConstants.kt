package com.moaga.app.data.utils

object ApiConstants {
    const val BASE_URL = "https://j13d105.p.ssafy.io/api/"
    const val TOKEN_KEY = "jwt_token"

    //SharedPreferences 파일의 이름
    const val PREFS_NAME = "moaga_prefs"

    // Timeouts
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // API Endpoints
    const val AUTH_LOGIN = "v1/auth/login"
    const val AUTH_EMAIL_SEND = "v1/auth/verify/email/send"
    const val AUTH_EMAIL_VERIFY = "v1/auth/verify/email/confirm"
    const val AUTH_SIGNUP = "v1/users/signup"
    const val AUTH_LOGIN_SIMPLE = "v1/auth/simple-login"
    const val EXPENSES = "expenses"
    const val TRANSACTIONS = "transactions/daily"
    const val PLANS = "plans"
    const val ANALYSIS = "analysis"
}