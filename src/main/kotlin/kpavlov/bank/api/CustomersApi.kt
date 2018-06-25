package kpavlov.bank.api

import kpavlov.bank.domain.CustomerDetails
import kpavlov.bank.domain.CustomerId
import java.util.concurrent.CompletionStage

interface CustomersApi {

    fun getCustomerDetails(customerId: CustomerId): CompletionStage<CustomerDetails>
}