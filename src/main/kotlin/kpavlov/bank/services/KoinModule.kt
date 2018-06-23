package kpavlov.bank.services

import akka.actor.ActorSystem
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import org.koin.dsl.module.applicationContext

private val actorSystem = ActorSystem.create("bank")

val KoinModule = applicationContext {
    bean { AccountsService(get()) } bind AccountsApi::class
    bean { CustomersService(get()) } bind CustomersApi::class bind CustomersService::class
    bean { TransactionService(get()) } bind TransactionService::class
    bean { actorSystem as ActorSystem }

    Bootstrap(actorSystem)
}

