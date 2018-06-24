package kpavlov.bank.services

import akka.actor.ActorSystem
import akka.pattern.PatternsCS.ask
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.services.actors.AccountCreatedEvt
import kpavlov.bank.services.actors.AccountStatementEvt
import kpavlov.bank.services.actors.CreateAccountCmd
import kpavlov.bank.services.actors.GetAccountStatementCmd
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.concurrent.CompletionStage

class AccountsService(actorSystem: ActorSystem) : AbstractAkkaService(actorSystem), AccountsApi {


    private val log = LoggerFactory.getLogger(AccountsService::class.java)

    override fun openAccount(customerId: CustomerId, initialCredit: BigDecimal, type: AccountType): CompletionStage<AccountCreatedEvt> {
        if (initialCredit.signum() < 0) {
            throw IllegalArgumentException("Initial balance should not be negative")
        }
        val initialBalanceCents = initialCredit.movePointRight(2).longValueExact()
        val actorSelection = lookupCustomerActor(customerId)
        val cmd = CreateAccountCmd(initialBalanceCents = initialBalanceCents, type = type)
        @Suppress("UNCHECKED_CAST")
        return ask(actorSelection, cmd, TIMEOUT) as CompletionStage<AccountCreatedEvt>
    }

    override fun getAccountStatement(customerId: CustomerId, accountId: AccountId): CompletionStage<AccountStatementEvt> {
        val actorSelection = lookupCustomerAccountActor(customerId, accountId)
        @Suppress("UNCHECKED_CAST")
        return ask(actorSelection, GetAccountStatementCmd(), TIMEOUT) as CompletionStage<AccountStatementEvt>
    }

}