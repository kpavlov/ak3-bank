package kpavlov.bank.services

import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.PatternsCS
import kpavlov.bank.api.CustomersApi
import kpavlov.bank.domain.Customer
import kpavlov.bank.domain.CustomerDetails
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.services.actors.CustomerActor
import kpavlov.bank.services.actors.GetCustomerDetailsCommand
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicInteger


class CustomersService(private val actorSystem: ActorSystem) : AbstractAkkaService(actorSystem), CustomersApi {

    companion object {
        private val customerIdSequence = AtomicInteger(0)
    }

    override fun createCustomer(customer: Customer): CompletionStage<CustomerDetails> {
        val newId = customerIdSequence.incrementAndGet()
        val props = Props.create(CustomerActor::class.java, newId, customer)
        val actorRef = actorSystem.actorOf(props, "customer-$newId")
        @Suppress("UNCHECKED_CAST")
        return PatternsCS.ask(actorRef, GetCustomerDetailsCommand(), TIMEOUT) as CompletionStage<CustomerDetails>
    }

    override suspend fun getCustomerDetails(customerId: CustomerId): CompletionStage<CustomerDetails> {
        val actorSelection = lookupCustomerActor(customerId)
        @Suppress("UNCHECKED_CAST")
        return PatternsCS.ask(actorSelection, GetCustomerDetailsCommand(), TIMEOUT) as CompletionStage<CustomerDetails>
    }
}
