package kpavlov.bank.rest

import io.ktor.locations.Location
import kpavlov.bank.domain.CustomerId

@Location("/customers/{customerId}")
data class GetCustomerRequest(val customerId: CustomerId)

