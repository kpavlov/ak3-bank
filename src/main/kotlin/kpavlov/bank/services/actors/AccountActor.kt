package kpavlov.bank.services.actors

import akka.actor.AbstractLoggingActor
import kpavlov.bank.api.model.Transaction
import kpavlov.bank.domain.AccountId

import java.time.Clock
import java.time.ZoneOffset

private val clock = Clock.systemUTC()

class GetStatementCmd

data class AccountBalanceUpdatedEvt(val balanceDeltaInCents: Long)

class AccountActor(private val id: AccountId) : AbstractLoggingActor() {

    private var balance: Long = 0
    private val transactions: MutableList<Transaction> = mutableListOf()

    override fun preStart() {
        log().info("Starting {}: {}", self)
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(CreateTransactionCmd::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    createTransaction(cmd)
                    sender.tell(AccountBalanceUpdatedEvt(cmd.amountCents), self)
                }
                .matchAny { o -> log().warning("received unknown message: {}", o) }
                .build()
    }

    private fun createTransaction(cmd: CreateTransactionCmd) {
        val tx = Transaction(
                accountId = id,
                amount = cmd.amountCents,
                timestamp = clock.instant().atOffset(ZoneOffset.UTC),
                counterpartyAccountRef = cmd.counterpartyAccountRef
        )
        transactions.add(tx)
        balance += cmd.amountCents
    }


}