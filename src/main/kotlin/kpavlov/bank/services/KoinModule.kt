package kpavlov.bank.services

import akka.actor.ActorSystem
import akka.event.EventStream
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import org.koin.dsl.module.applicationContext

private val actorSystem = ActorSystem.create("bank")

val KoinModule = applicationContext {
    //    factory { MyPresenter(get()) } // get() will resolve Repository instance
    bean { AccountsService(get(), get()) } bind AccountsApi::class
    bean { CustomersService(get()) } bind CustomersApi::class bind CustomersService::class
    bean { TransactionService(get()) } bind TransactionService::class
    bean { actorSystem as ActorSystem }
    bean { actorSystem.eventStream() as EventStream }

    Bootstrap(actorSystem)
}

