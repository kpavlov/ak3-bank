package kpavlov.bank.services

import akka.actor.ActorSystem
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import org.koin.dsl.module.applicationContext

val KoinModule = applicationContext {

    val actorSystem = ActorSystem.create("bank")

    bean { AccountsService(actorSystem) } bind AccountsApi::class
    bean { CustomersService(actorSystem) } bind CustomersApi::class bind CustomersService::class
    bean { TransactionService(actorSystem) } bind TransactionService::class
}

