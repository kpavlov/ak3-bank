package kpavlov.bank.api

import kpavlov.bank.domain.CustomerId
import kpavlov.bank.services.actors.AccountCreatedEvt
import java.math.BigDecimal
import java.util.concurrent.CompletionStage

interface AccountsApi {

    fun openAccount(customerId: CustomerId, initialCredit: BigDecimal): CompletionStage<AccountCreatedEvt>
}