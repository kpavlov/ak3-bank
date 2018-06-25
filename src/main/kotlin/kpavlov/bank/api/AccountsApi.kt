package kpavlov.bank.api

import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.CustomerId
import java.math.BigDecimal
import java.util.concurrent.CompletionStage

interface AccountsApi {

    fun openAccount(customerId: CustomerId,
                    initialCredit: BigDecimal = BigDecimal.ZERO,
                    type: AccountType = AccountType.CURRENT): CompletionStage<AccountCreatedEvt>

    fun getAccountStatement(customerId: CustomerId, accountId: AccountId): CompletionStage<AccountStatementEvt>
}