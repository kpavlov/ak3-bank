package kpavlov.bank.rest

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.routing
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import org.koin.ktor.ext.inject

fun Application.mainModule() {

    install(Locations)
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {}
    }
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, """
                          {"error": "${cause.message}"}
                """.trimIndent())
        }
    }

    val accountsApi by inject<AccountsApi>()
    val customersApi by inject<CustomersApi>()

    routing {
        root(accountsApi, customersApi)
    }
}

