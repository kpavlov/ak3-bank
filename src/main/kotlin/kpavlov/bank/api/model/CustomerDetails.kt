package kpavlov.bank.api.model

import kpavlov.bank.domain.CustomerId
import java.math.BigDecimal

data class CustomerDetails(
        val id: CustomerId,
        val firstName: String,
        val lastName: String,
        val balance: BigDecimal,
        val accounts: List<AccountStatement>
)