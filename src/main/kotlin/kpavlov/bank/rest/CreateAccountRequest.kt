package kpavlov.bank.rest

import io.ktor.locations.Location
import kpavlov.bank.domain.CustomerId
import java.math.BigDecimal

@Location("/customers/{customerId}/accounts")
data class CreateAccountRequest(
        val customerId: CustomerId,
        val initialCredit: String = "0") {

    fun getInitialCredit(): BigDecimal {
        return BigDecimal(initialCredit)
    }
}