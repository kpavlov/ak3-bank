package kpavlov.bank.services.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountStatement
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.Transaction
import java.time.Clock
import java.time.ZoneOffset
import java.util.*

private val clock = Clock.systemUTC()

class GetAccountStatementCmd

data class AccountBalanceUpdatedEvt(val id: AccountId,
                                    val balanceDeltaInCents: Long,
                                    val balanceInCents: Long)

data class AccountStatementEvt(val accountStatement: AccountStatement)

class AccountActor(private val id: AccountId, private val type: AccountType) : AbstractLoggingActor() {

    private var balance: Long = 0
    private val transactions: MutableList<Transaction> = mutableListOf()

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(CreateTransactionCmd::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    createTransaction(cmd, sender)
                }
                .match(GetAccountStatementCmd::class.java) { cmd ->
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

    private fun createTransaction(cmd: CreateTransactionCmd, toNotify: ActorRef) {
        val tx = Transaction(
                id = UUID.randomUUID(),
                accountId = id,
                amount = cmd.amountCents,
                timestamp = clock.instant().atOffset(ZoneOffset.UTC),
                counterpartyAccountRef = cmd.counterpartyAccountRef
        )
        transactions.add(tx)
        balance += cmd.amountCents

        val updateEvt = AccountBalanceUpdatedEvt(
                id = id,
                balanceDeltaInCents = cmd.amountCents,
                balanceInCents = balance)

        toNotify.tell(updateEvt, self)
    }


}