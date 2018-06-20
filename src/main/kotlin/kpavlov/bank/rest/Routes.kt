package kpavlov.bank.rest

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.withContext
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi

fun Routing.root(accountsApi: AccountsApi, customersApi: CustomersApi) {

    val computeContext = newFixedThreadPoolContext(10, "compute")

    get<Any> {
        call.respondText("OK", ContentType.Text.Plain)
    }

    get<GetCustomerRequest> {
        val customerId = it.customerId
        withContext(computeContext) {
            val details = customersApi.getCustomerDetails(customerId).await()
            if (details != null) {
                call.respond(details)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    post<CreateAccountRequest> {
        it.initialCredit
        withContext(computeContext) {
            val accountId = accountsApi.openAccount(it.customerId, it.initialCredit)

            if (accountId != null) {
                val details = customersApi.getCustomerDetails(it.customerId)
                call.respond(HttpStatusCode.Created, details)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

}
