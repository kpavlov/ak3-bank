package kpavlov.bank.domain

import java.time.OffsetDateTime

data class AccountStatement(
        val id: AccountId,
        val type: AccountType,
        val balance: Long,
        val timestamp: OffsetDateTime,
        val transactions: List<Transaction>
)