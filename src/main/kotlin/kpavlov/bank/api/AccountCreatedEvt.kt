package kpavlov.bank.api

import kpavlov.bank.domain.AccountId

data class AccountCreatedEvt(val accountId: AccountId, val customerBalanceCents: Long)