package kpavlov.bank.services

import akka.actor.ActorSystem
import akka.pattern.PatternsCS.ask
import kpavlov.bank.api.AccountCreatedEvt
import kpavlov.bank.api.AccountStatementEvt
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.services.actors.CreateAccountCommand
import kpavlov.bank.services.actors.GetAccountStatementCommand
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.concurrent.CompletionStage

class AccountsService(actorSystem: ActorSystem) : AbstractAkkaService(actorSystem), AccountsApi {

    private val log = LoggerFactory.getLogger(AccountsService::class.java)

    override suspend fun openAccount(customerId: CustomerId, type: AccountType, initialCredit: BigDecimal): CompletionStage<AccountCreatedEvt> {
        if (initialCredit.signum() < 0) {
            throw IllegalArgumentException("Initial balance should not be negative")
        }
        log.info("Opening {} account for customer #{} with balance={}", type, customerId, initialCredit)
        val initialBalanceCents = initialCredit.movePointRight(2).longValueExact()
        val actorSelection = lookupCustomerActor(customerId)
        val cmd = CreateAccountCommand(initialBalanceCents = initialBalanceCents, type = type)
        @Suppress("UNCHECKED_CAST")
        return ask(actorSelection, cmd, TIMEOUT) as CompletionStage<AccountCreatedEvt>
    }

    override suspend fun getAccountStatement(customerId: CustomerId, accountId: AccountId): CompletionStage<AccountStatementEvt> {
        log.info("Requesting account statement for customer #{} accountId#{}", customerId, accountId)
        val actorSelection = lookupCustomerAccountActor(customerId, accountId)
        @Suppress("UNCHECKED_CAST")
        return ask(actorSelection, GetAccountStatementCommand(), TIMEOUT) as CompletionStage<AccountStatementEvt>
    }

}