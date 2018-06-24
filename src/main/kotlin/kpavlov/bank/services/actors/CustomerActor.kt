package kpavlov.bank.services.actors

import akka.actor.AbstractActor
import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.PatternsCS
import kpavlov.bank.api.model.AccountStatement
import kpavlov.bank.api.model.CustomerDetails
import kpavlov.bank.domain.*
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch

data class CreateAccountCmd(val initialBalanceCents: Long = 0,
                            val type: AccountType = AccountType.CURRENT)

class GetCustomerDetailsCmd
data class CustomerBalanceUpdatedEvt(val customerId: CustomerId, val newBalanceInCents: Long)
data class AccountCreatedEvt(val accountId: AccountId, val customerBalanceCents: Long)

data class CreateTransactionCmd(
        val accountId: AccountId,
        val amountCents: Long,
        val counterpartyAccountRef: AccountRef? = null
)

class CustomerActor(private var info: Customer) : AbstractLoggingActor() {

    private val accounts: MutableMap<AccountId, ActorRef> = mutableMapOf()
    private var balanceCents: Long = 0

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(GetCustomerDetailsCmd::class.java) { s ->
                    log().info("Received {}", s)
                    sender.tell(createCustomerDetails(), self)
                }
                .match(CreateAccountCmd::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    createAccount(sender, cmd)
                }
                .match(AccountBalanceUpdatedEvt::class.java) { evt ->
                    onAccountBalanceUpdated(evt)
                }
                .match(CreateTransactionCmd::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    val actorRef = accounts[cmd.accountId]
                    actorRef?.tell(cmd, self)
                }
                .matchAny { o -> log().warning("received unknown message: {}", o) }
                .build()
    }

    private fun onAccountBalanceUpdated(evt: AccountBalanceUpdatedEvt, toNotify: ActorRef? = null) {
        log().debug("Account balance updated. Updating customer balance. {}", evt)
        balanceCents += evt.balanceDeltaInCents
        val balanceUpdatedEvt = CustomerBalanceUpdatedEvt(info.id, balanceCents)
        toNotify?.let {
            it.tell(balanceUpdatedEvt, self)
        }
        context.system.eventStream().publish(balanceUpdatedEvt)
    }

    private fun createAccount(sender: ActorRef, cmd: CreateAccountCmd): AccountId {
        val newAccountId = accounts.size + 1
        val props = Props.create(AccountActor::class.java, newAccountId, cmd.type)
        val accountActorRef = context.actorOf(props, "account-$newAccountId")
        accounts[newAccountId] = accountActorRef

        if (cmd.initialBalanceCents > 0) {
            val depositCmd = CreateTransactionCmd(newAccountId, cmd.initialBalanceCents)
            PatternsCS.ask(accountActorRef, depositCmd, ACTOR_TIMEOUT)
                    .whenComplete { evt, t ->
                        handleActorResponse(t, "Can't deposit initial balance",
                                evt, AccountBalanceUpdatedEvt::class.java) { e ->
                            onAccountBalanceUpdated(e)
                            sender.tell(AccountCreatedEvt(newAccountId, e.balanceInCents), self)
                        }
                    }
        } else {
            sender.tell(AccountCreatedEvt(newAccountId, 0), self)
        }
        return newAccountId
    }

    private fun createCustomerDetails(): CustomerDetails {
        val countDownLatch = CountDownLatch(accounts.size)
        val accountDetails = mutableListOf<AccountStatement>()
        for (entry in accounts) {
            PatternsCS.ask(entry.value, GetAccountStatementCmd(), ACTOR_TIMEOUT)
                    .whenComplete { evt, t ->
                        handleActorResponse(t, "Can't request account statement",
                                evt, AccountStatementEvt::class.java) { e ->
                            accountDetails.add(e.accountStatement)
                            countDownLatch.countDown()
                        }
                    }
        }
        countDownLatch.await()
        accountDetails.sortBy { it.id }

        return CustomerDetails(
                id = info.id,
                firstName = info.firstName,
                lastName = info.lastName,
                balance = BigDecimal(balanceCents).movePointLeft(2).setScale(2),// not always 2
                accounts = accountDetails
        )
    }
}