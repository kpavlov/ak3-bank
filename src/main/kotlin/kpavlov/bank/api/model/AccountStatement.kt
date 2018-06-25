package kpavlov.bank.api.model

import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountType
import java.time.OffsetDateTime

data class AccountStatement(
        val id: AccountId,
        val type: AccountType,
        val balance: Long,
        val timestamp: OffsetDateTime,
        val transactions: List<Transaction>
)