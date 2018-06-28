package kpavlov.bank.api

import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.CustomerId
import java.math.BigDecimal
import java.util.concurrent.CompletionStage

interface AccountsApi {

    suspend fun openAccount(customerId: CustomerId,
                            type: AccountType = AccountType.CURRENT,
                            initialCredit: BigDecimal = BigDecimal.ZERO): CompletionStage<AccountCreatedEvt>

    suspend fun getAccountStatement(customerId: CustomerId, accountId: AccountId): CompletionStage<AccountStatementEvt>
}