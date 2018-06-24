package kpavlov.bank

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kpavlov.bank.rest.mainModule
import kpavlov.bank.services.KoinModule
import org.koin.standalone.StandAloneContext.startKoin

fun main(args: Array<String>) {

    startKoin(listOf(KoinModule))

    embeddedServer(Netty, port = 8080, module = Application::mainModule)
            .start(wait = true)

}