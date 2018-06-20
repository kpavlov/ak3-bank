package kpavlov.bank.services

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import kpavlov.bank.domain.Customer
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.services.actors.CustomerActor

class Bootstrap(private val actorSystem: ActorSystem) {

    init {
        createCustomersCustomer()
    }

    private fun createCustomersCustomer() {

        createCustomer(
                id = "1",
                firstName = "Tirion",
                lastName = "Lannister"
        )

        createCustomer(
                id = "2",
                firstName = "Bronn",
                lastName = "of the Blackwater"
        )
    }

    private fun createCustomer(id: CustomerId,
                               firstName: String,
                               lastName: String) {
        val props = Props.create(CustomerActor::class.java, Customer(
                id = id,
                firstName = firstName,
                lastName = lastName
        ))
        actorSystem.actorOf(props, "customer-${id}")
                .tell("Hi", ActorRef.noSender())
    }

}