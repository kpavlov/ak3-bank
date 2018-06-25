package kpavlov.bank.services.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import kpavlov.bank.api.AccountStatementEvt
import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountStatement
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.Transaction
import java.time.Clock
import java.time.ZoneOffset
import java.util.*

private val clock = Clock.systemUTC()

class GetAccountStatementCommand

data class AccountBalanceUpdatedEvent(val id: AccountId,
                                      val balanceDeltaInCents: Long,
                                      val balanceInCents: Long)

class AccountActor(private val id: AccountId, private val type: AccountType) : AbstractLoggingActor() {

    private var balance: Long = 0
    private val transactions: MutableList<Transaction> = mutableListOf()

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(CreateTransactionCommand::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    createTransaction(cmd, sender)
                }
                .match(GetAccountStatementCommand::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    val transactionsDefensiveCopy = transactions
                            .map { it -> it.copy() }
                            .toList()

                    val statementEvt = AccountStatementEvt(
                            AccountStatement(id = id,
                                    type = type,
                                    balance = balance,
                                    transactions = transactionsDefensiveCopy,
                                    timestamp = clock.instant().atOffset(ZoneOffset.UTC)
                            ))
                    sender.tell(statementEvt, self)
                }
                .matchAny { o -> log().warning("received unknown message: {}", o) }
                .build()
    }

    private fun createTransaction(cmd: CreateTransactionCommand, toNotify: ActorRef) {
        val tx = Transaction(
                id = UUID.randomUUID(),
                accountId = id,
                amount = cmd.amountCents,
                timestamp = clock.instant().atOffset(ZoneOffset.UTC),
                counterpartyAccountRef = cmd.counterpartyAccountRef
        )
        transactions.add(tx)
        balance += cmd.amountCents

        val updateEvt = AccountBalanceUpdatedEvent(
                id = id,
                balanceDeltaInCents = cmd.amountCents,
                balanceInCents = balance)

        toNotify.tell(updateEvt, self)
    }


}