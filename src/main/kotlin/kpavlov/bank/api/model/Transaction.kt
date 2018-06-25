package kpavlov.bank.api.model

import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountRef
import kpavlov.bank.domain.TransactionId
import java.time.OffsetDateTime

data class Transaction(
        val id: TransactionId,
        val accountId: AccountId,
        val amount: Long,
        val timestamp: OffsetDateTime,
        val counterpartyAccountRef: AccountRef?
)