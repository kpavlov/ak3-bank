package kpavlov.bank.domain

data class CustomerDetails(
        val id: CustomerId,
        val firstName: String,
        val lastName: String,
        val balance: Long,
        val accounts: List<AccountStatement>
)