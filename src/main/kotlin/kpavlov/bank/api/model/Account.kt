package kpavlov.bank.api.model

import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.CustomerId

data class Account(
        val id: AccountId,
        val customerId: CustomerId,
        val type: AccountType,
        val transactions: List<Transaction>
)