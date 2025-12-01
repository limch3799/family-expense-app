package com.moaga.app.data.api.dto.request

data class FinancialConnectRequest(
    val groupId: Long,
    val accountIds: List<Long>,
    val cardIds: List<Long>
)

data class FinancialDisconnectRequest(
    val groupId: Long,
    val accountId: Long?,
    val cardId: Long?
)