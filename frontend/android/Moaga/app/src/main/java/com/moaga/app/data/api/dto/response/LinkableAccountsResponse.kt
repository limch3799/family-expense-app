package com.moaga.app.data.api.dto.response

data class LinkableAccountsResponse(
    val accounts: List<AccountDto>,
    val cards: List<CardDto>,
    val message: String
)

data class AccountDto(
    val accountId: Long,
    val accountNo: String,
    val bankName: String,
    val isConnectedToGroup: Boolean
)

data class CardDto(
    val cardId: Long,
    val cardNo: String,
    val cardCompany: String,
    val isConnectedToGroup: Boolean
)