package kpavlov.bank.api

import kpavlov.bank.api.model.CustomerDetails
import kpavlov.bank.domain.CustomerId
import java.util.concurrent.CompletionStage

interface CustomersApi {

    fun getCustomerDetails(customerId: CustomerId): CompletionStage<CustomerDetails>
}