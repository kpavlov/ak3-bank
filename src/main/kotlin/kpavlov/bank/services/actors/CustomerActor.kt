package kpavlov.bank.services.actors

import akka.actor.AbstractActor
import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.actor.Props
import kpavlov.bank.api.model.CustomerDetails
import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.AccountRef
import kpavlov.bank.domain.Customer
import kpavlov.bank.domain.CustomerId
import java.math.BigDecimal

data class CreateAccountCmd(val initialBalanceCents: Long = 0)
class GetCustomerDetailsCmd
data class CustomerBalanceUpdatedEvt(val customerId: CustomerId, val newBalanceInCents: Long)
data class AccountCreatedEvt(val accountId: AccountId)

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
                    log().info("Received {}", evt)
                    balanceCents += evt.balanceDeltaInCents
                    context.system.eventStream().publish(CustomerBalanceUpdatedEvt(info.id, balanceCents))
                }
                .match(CreateTransactionCmd::class.java) { cmd ->
                    log().info("Received {}", cmd)
                    val actorRef = accounts[cmd.accountId]
                    actorRef?.tell(cmd, self)
                }
                .matchAny { o -> log().warning("received unknown message: {}", o) }
                .build()
    }

    private fun createAccount(sender: ActorRef, cmd: CreateAccountCmd): AccountId {
        val newAccountId = accounts.size + 1
        val props = Props.create(AccountActor::class.java, newAccountId)
        val actorRef = context.actorOf(props, "account-$newAccountId")
        accounts[newAccountId] = actorRef

        sender.tell(AccountCreatedEvt(newAccountId), self)

        if (cmd.initialBalanceCents > 0) {
            actorRef.tell(CreateTransactionCmd(newAccountId, cmd.initialBalanceCents), self)
        }

        return newAccountId
    }

    private fun createCustomerDetails(): CustomerDetails {
        for (entry in accounts) {
//            PatternsCS.ask(entry.value, GetStatementCmd(), )
        }

        return CustomerDetails(
                id = info.id,
                firstName = info.firstName,
                lastName = info.lastName,
                balance = BigDecimal(balanceCents).movePointLeft(2).setScale(2),// not always 2
                accounts = emptyList()
        )

    }
}