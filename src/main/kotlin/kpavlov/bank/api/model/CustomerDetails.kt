package kpavlov.bank.api.model

import kpavlov.bank.domain.CustomerId

data class CustomerDetails(
        val id: CustomerId,
        val firstName: String,
        val lastName: String,
        val balance: Long,
        val accounts: List<AccountStatement>
)