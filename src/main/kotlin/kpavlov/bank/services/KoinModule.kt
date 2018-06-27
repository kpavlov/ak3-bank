package kpavlov.bank.services

import akka.actor.ActorSystem
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import org.koin.dsl.module.applicationContext

private val actorSystem = ActorSystem.create("bank")

val KoinModule = applicationContext {

    val customersService = CustomersService(actorSystem)

    bean { AccountsService(actorSystem) } bind AccountsApi::class
    bean { customersService } bind CustomersApi::class bind CustomersService::class
    bean { TransactionService(actorSystem) } bind TransactionService::class

    Bootstrap(customersService)
}

