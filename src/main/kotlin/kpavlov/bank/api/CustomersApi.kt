package kpavlov.bank.api

import kpavlov.bank.domain.Customer
import kpavlov.bank.domain.CustomerDetails
import kpavlov.bank.domain.CustomerId
import java.util.concurrent.CompletionStage

interface CustomersApi {

    fun createCustomer(customer: Customer): CompletionStage<CustomerDetails>
    suspend fun getCustomerDetails(customerId: CustomerId): CompletionStage<CustomerDetails>
}