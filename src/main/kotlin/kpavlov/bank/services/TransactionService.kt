package kpavlov.bank.services

import akka.actor.ActorSystem
import akka.pattern.PatternsCS
import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountRef
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.domain.Transaction
import kpavlov.bank.services.actors.CreateTransactionCommand
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.concurrent.CompletionStage

class TransactionService(private val actorSystem: ActorSystem) : AbstractAkkaService(actorSystem = actorSystem) {


    private val log = LoggerFactory.getLogger(TransactionService::class.java)

    fun depositWithdraw(customerId: CustomerId, accountId: AccountId, amount: BigDecimal): CompletionStage<Transaction> {
        log.info("Depositing {} to the account {}/{}", amount, customerId, accountId)
        return transfer(customerId, accountId, amount, null)
    }

    fun transfer(customerId: CustomerId,
                 accountId: AccountId,
                 amount: BigDecimal,
                 counterpartyAccountRef: AccountRef?): CompletionStage<Transaction> {
        val customerActorSelection = lookupCustomerActor(customerId)
        val cmd = CreateTransactionCommand(
                accountId = accountId,
                amountCents = amount.movePointRight(2).longValueExact(),
                counterpartyAccountRef = counterpartyAccountRef
        )
        @Suppress("UNCHECKED_CAST")
        return PatternsCS.ask(customerActorSelection, cmd, TIMEOUT) as CompletionStage<Transaction>
    }
}