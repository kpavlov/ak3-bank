package kpavlov.bank.services.actors

import akka.actor.AbstractActor
import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.PatternsCS
import kpavlov.bank.api.AccountCreatedEvt
import kpavlov.bank.api.AccountStatementEvt
import kpavlov.bank.domain.*
import java.util.concurrent.CountDownLatch

data class CreateAccountCommand(val initialBalanceCents: Long = 0,
                                val type: AccountType = AccountType.CURRENT)

class GetCustomerDetailsCommand

data class CustomerBalanceUpdatedEvent(val customerId: CustomerId, val newBalanceInCents: Long)

data class CreateTransactionCommand(
        val accountId: AccountId,
        val amountCents: Long,
        val counterpartyAccountRef: AccountRef? = null
)

class CustomerActor(private var info: Customer) : AbstractLoggingActor() {

    private val accounts: MutableMap<AccountId, ActorRef> = mutableMapOf()
    private var balanceCents: Long = 0

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(GetCustomerDetailsCommand::class.java) { s ->
                    log().info("Received {}", s)
                    sender.tell(createCustomerDetails(), self)
                }
                .match(CreateAccountCommand::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    createAccount(sender, cmd)
                }
                .match(AccountBalanceUpdatedEvent::class.java) { evt ->
                    onAccountBalanceUpdated(evt)
                }
                .match(CreateTransactionCommand::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    val actorRef = accounts[cmd.accountId]
                    actorRef?.tell(cmd, self)
                }
                .matchAny { o -> log().warning("received unknown message: {}", o) }
                .build()
    }

    private fun onAccountBalanceUpdated(evt: AccountBalanceUpdatedEvent, toNotify: ActorRef? = null) {
        log().debug("Account balance updated. Updating customer balance. {}", evt)
        balanceCents += evt.balanceDeltaInCents
        val balanceUpdatedEvt = CustomerBalanceUpdatedEvent(info.id, balanceCents)
        toNotify?.tell(balanceUpdatedEvt, self)
        context.system.eventStream().publish(balanceUpdatedEvt)
    }

    private fun createAccount(sender: ActorRef, cmd: CreateAccountCommand): AccountId {
        val newAccountId = accounts.size + 1
        val props = Props.create(AccountActor::class.java, newAccountId, cmd.type)
        val accountActorRef = context.actorOf(props, "account-$newAccountId")
        accounts[newAccountId] = accountActorRef

        if (cmd.initialBalanceCents > 0) {
            val depositCmd = CreateTransactionCommand(newAccountId, cmd.initialBalanceCents)
            PatternsCS.ask(accountActorRef, depositCmd, ACTOR_TIMEOUT)
                    .whenComplete { evt, t ->
                        handleActorResponse(t, "Can't deposit initial balance",
                                evt, AccountBalanceUpdatedEvent::class.java) { e ->
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
            PatternsCS.ask(entry.value, GetAccountStatementCommand(), ACTOR_TIMEOUT)
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
                balance = balanceCents,
                accounts = accountDetails
        )
    }
}