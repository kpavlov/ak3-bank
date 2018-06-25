package kpavlov.bank.rest

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.withContext
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import kpavlov.bank.domain.AccountId
import kpavlov.bank.domain.CustomerId


fun Routing.root(accountsApi: AccountsApi, customersApi: CustomersApi) {

    val computeContext = newFixedThreadPoolContext(10, "compute")

    get<Any> {
        call.respondText("OK", ContentType.Text.Plain)
    }

    get<CustomerLocation> {
        val customerId = it.customerId
        withContext(computeContext) {
            val details = customersApi.getCustomerDetails(customerId).await()
            call.respond(convertCustomerDetails(details))
        }
    }

    post<CustomerAccountsLocation> {
        val req = call.receive<CreateAccountRequest>()
        withContext(computeContext) {
            val evt = accountsApi.openAccount(it.customerId, req.initialCredit, req.type).await()
            call.response.header(HttpHeaders.Location, "/customers/${it.customerId}/accounts/${evt.accountId}")
            call.respond(HttpStatusCode.Created)
        }
    }

    get<CustomerAccountLocation> {
        withContext(computeContext) {
            val evt = accountsApi.getAccountStatement(it.customerId, it.accountId).await()
            call.response.header(HttpHeaders.Location, "/customers/${it.customerId}/accounts/${it.accountId}")
            call.respond(HttpStatusCode.OK, convertAccountStatement(evt.accountStatement))
        }
    }

}

@Location("/v1/customers/{customerId}")
data class CustomerLocation(val customerId: CustomerId)

@Location("/v1/customers/{customerId}/accounts")
data class CustomerAccountsLocation(val customerId: CustomerId)

@Location("/v1/customers/{customerId}/accounts/{accountId}")
data class CustomerAccountLocation(val customerId: CustomerId, val accountId: AccountId)
