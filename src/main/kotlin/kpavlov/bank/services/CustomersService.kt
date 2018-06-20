package kpavlov.bank.services

import akka.actor.ActorSystem
import akka.pattern.PatternsCS
import kpavlov.bank.api.CustomersApi
import kpavlov.bank.api.model.CustomerDetails
import kpavlov.bank.services.actors.GetCustomerDetailsCmd
import java.util.concurrent.CompletionStage


class CustomersService(actorSystem: ActorSystem) : AbstractAkkaService(actorSystem), CustomersApi {

    override fun getCustomerDetails(customerId: String): CompletionStage<CustomerDetails> {
        val actorSelection = lookupCustomerActor(customerId)
        return PatternsCS.ask(actorSelection, GetCustomerDetailsCmd(), TIMEOUT) as CompletionStage<CustomerDetails>
    }
}
