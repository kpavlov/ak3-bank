package kpavlov.bank

import com.devskiller.jfairy.Fairy
import kpavlov.bank.domain.*
import java.time.Clock
import java.time.ZoneOffset
import java.util.*


object TestObjectMother {

    private var fairy = Fairy.create()

    fun createRandomTransaction(accountId: AccountId): Transaction {
        return Transaction(
                id = UUID.randomUUID(),
                accountId = accountId,
                amount = (1..1000_00).random().toLong(),
                timestamp = Clock.systemUTC().instant().atOffset(ZoneOffset.UTC),
                counterpartyAccountRef = null
        )
    }

    fun createRandomAccountStatement(): AccountStatement {
        val id: AccountId = (1..1000_00).random()
        return AccountStatement(
                id = id,
                type = AccountType.values()[(0..1).random()],
                balance = (1..1000_00).random().toLong(),
                timestamp = Clock.systemUTC().instant().atOffset(ZoneOffset.UTC),
                transactions = (0..10).map { createRandomTransaction(id) }.toList()
        )
    }

    fun createRandomCustomerDetails(): CustomerDetails {
        val person = fairy.person()
        return CustomerDetails(
                id = (10..100).random(),
                firstName = person.firstName,
                lastName = person.lastName,
                balance = (1..1000_00).random().toLong(),
                accounts = (0..3).map { createRandomAccountStatement() }.toList()

        )
    }
}