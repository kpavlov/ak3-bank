package kpavlov.bank.api

import kpavlov.bank.domain.CustomerId
import java.math.BigDecimal
import java.util.concurrent.CompletionStage

interface AccountsApi {

    fun openAccount(customerId: CustomerId, initialCredit: BigDecimal): CompletionStage<Any>
}