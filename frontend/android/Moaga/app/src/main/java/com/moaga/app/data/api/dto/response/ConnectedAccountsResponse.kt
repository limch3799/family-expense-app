package com.moaga.app.data.api.dto.response

data class ConnectedAccountsResponse(
    val myConnectedAccounts: List<ConnectedAccountDto>,
    val myConnectedCards: List<ConnectedCardDto>,
    val message: String
)

data class ConnectedAccountDto(
    val accountId: Long,
    val accountNo: String,
    val bankName: String
)

data class ConnectedCardDto(
    val cardId: Long,
    val cardNo: String,
    val cardCompany: String
)