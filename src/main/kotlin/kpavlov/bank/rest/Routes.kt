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
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.Customer
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.rest.v1.model.CreateAccountRequest
import kpavlov.bank.rest.v1.model.CreateCustomerRequest
import java.math.BigDecimal


fun Routing.root(accountsApi: AccountsApi, customersApi: CustomersApi) {

    val computeContext = newFixedThreadPoolContext(10, "compute")

    get<Any> {
        call.respondText("OK", ContentType.Text.Plain)
    }

    post<Customers> {
        val req = call.receive<CreateCustomerRequest>()
        withContext(computeContext) {
            val details = customersApi.createCustomer(
                    Customer(firstName = req.firstName, lastName = req.lastName)
            ).await()
            call.response.header(HttpHeaders.Location, "/customers/${details.id}")
            call.respond(HttpStatusCode.Created, convertCustomerDetails(details))
        }
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
        val type = when (req.type) {
            kpavlov.bank.rest.v1.model.AccountType.SAVINGS -> AccountType.SAVINGS
            kpavlov.bank.rest.v1.model.AccountType.CURRENT -> AccountType.CURRENT
            null -> AccountType.CURRENT
        }
        withContext(computeContext) {
            val evt = accountsApi.openAccount(it.customerId, req.initialCredit ?: BigDecimal.ZERO, type).await()
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

@Location("/v1/customers")
class Customers

@Location("/v1/customers/{customerId}")
data class CustomerLocation(val customerId: CustomerId)

@Location("/v1/customers/{customerId}/accounts")
data class CustomerAccountsLocation(val customerId: CustomerId)

@Location("/v1/customers/{customerId}/accounts/{accountId}")
data class CustomerAccountLocation(val customerId: CustomerId, val accountId: AccountId)
