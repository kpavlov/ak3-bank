package kpavlov.bank.rest

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.header
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
            call.respond(details)
        }
    }

    post<CreateAccountRequest> {
        it.initialCredit
        withContext(computeContext) {
            val evt = accountsApi.openAccount(it.customerId, it.getInitialCredit(), it.getAccountType()).await()
            call.response.header(HttpHeaders.Location, "/customers/${it.customerId}/accounts/${evt.accountId}")
            call.respond(HttpStatusCode.Created)
        }
    }

}
