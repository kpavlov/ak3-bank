package kpavlov.bank.domain

import java.time.OffsetDateTime

data class Transaction(
        val id: TransactionId,
        val accountId: AccountId,
        val amount: Long,
        val timestamp: OffsetDateTime,
        val counterpartyAccountRef: AccountRef?
)