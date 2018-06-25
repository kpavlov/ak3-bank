package kpavlov.bank.rest

import kpavlov.bank.rest.v1.model.AccountType

internal fun convertCustomerDetails(src: kpavlov.bank.api.model.CustomerDetails): kpavlov.bank.rest.v1.model.CustomerDetails {
    return kpavlov.bank.rest.v1.model.CustomerDetails(
            id = src.id,
            firstName = src.firstName,
            lastName = src.lastName,
            balance = src.balance.toBigDecimal().movePointLeft(2),
            accounts = src.accounts.map { convertAccountStatement(it) }.toTypedArray()
    )
}

internal fun convertAccountStatement(src: kpavlov.bank.api.model.AccountStatement): kpavlov.bank.rest.v1.model.AccountStatement {
    return kpavlov.bank.rest.v1.model.AccountStatement(
            id = src.id,
            type = AccountType.valueOf(src.type.name),
            balance = src.balance.toBigDecimal().movePointLeft(2),
            timestamp = src.timestamp,
            transactions = src.transactions.map { convertTransaction(it) }.toTypedArray()
    )
}

private fun convertTransaction(src: kpavlov.bank.api.model.Transaction): kpavlov.bank.rest.v1.model.Transaction {
    return kpavlov.bank.rest.v1.model.Transaction(
            id = src.id,
            accountId = src.accountId,
            amount = src.amount.toBigDecimal().movePointLeft(2),
            timestamp = src.timestamp
    )
}