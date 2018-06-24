package kpavlov.bank.rest

import kpavlov.bank.domain.AccountType
import java.math.BigDecimal

data class CreateAccountRequest(val initialCredit: BigDecimal = BigDecimal.ZERO,
                                val type: AccountType = AccountType.CURRENT)