package kpavlov.bank.api

import kpavlov.bank.domain.AccountStatement

data class AccountStatementEvt(val accountStatement: AccountStatement)