package kpavlov.bank.services

import akka.actor.ActorSystem
import akka.pattern.PatternsCS.ask
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.services.actors.CreateAccountCmd
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.concurrent.CompletionStage

class AccountsService(actorSystem: ActorSystem, private val transactionService: TransactionService) : AbstractAkkaService(actorSystem), AccountsApi {

    private val log = LoggerFactory.getLogger(AccountsService::class.java)

    override fun openAccount(customerId: CustomerId, initialCredit: BigDecimal): CompletionStage<Any> {
        if (initialCredit.signum() < 0) {
            throw IllegalArgumentException("Initial balance should not be negative")
        }
        val actorSelection = lookupCustomerActor(customerId)
        val initialBalanceCents = initialCredit.movePointRight(2).longValueExact()
        return ask(actorSelection, CreateAccountCmd(initialBalanceCents = initialBalanceCents), TIMEOUT)
    }


}