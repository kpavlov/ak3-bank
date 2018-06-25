package kpavlov.bank.rest

import io.kotlintest.shouldBe
import kpavlov.bank.TestObjectMother
import kpavlov.bank.rest.v1.model.AccountStatement
import kpavlov.bank.rest.v1.model.CustomerDetails
import kpavlov.bank.rest.v1.model.Transaction
import org.junit.Test
import org.koin.test.KoinTest

class DataConvertersTest : KoinTest {

    @Test
    fun shouldConvertCustomerDetails() {
        // given
        val src = TestObjectMother.createRandomCustomerDetails()

        //when
        val result = convertCustomerDetails(src)

        //then
        assertCustomerDetails(result, src)
    }

    private fun assertCustomerDetails(actual: CustomerDetails, src: kpavlov.bank.domain.CustomerDetails) {
        with(actual) {
            id shouldBe src.id
            firstName shouldBe src.firstName
            lastName shouldBe src.lastName
            balance shouldBe src.balance.toBigDecimal().movePointLeft(2)
            accounts.size shouldBe src.accounts.size
            for (i in 0 until accounts.size) {
                assertAccountStatement(accounts[i], src.accounts[i])
            }
        }
    }

    @Test
    fun shouldConvertAccountStatement() {
        // given
        val src = TestObjectMother.createRandomAccountStatement()

        //when
        val result = convertAccountStatement(src)

        //then
        assertAccountStatement(result, src)
    }

    private fun assertAccountStatement(actual: AccountStatement, src: kpavlov.bank.domain.AccountStatement) {
        with(actual) {
            id shouldBe src.id
            type.name shouldBe src.type.name
            balance shouldBe src.balance.toBigDecimal().movePointLeft(2)
            timestamp shouldBe src.timestamp
            transactions.size shouldBe src.transactions.size
            for (i in 0 until transactions.size) {
                assertTransaction(transactions[i], src.transactions[i])
            }
        }
    }

    private fun assertTransaction(actual: Transaction, src: kpavlov.bank.domain.Transaction) {
        with(actual) {
            id shouldBe src.id
            accountId shouldBe src.accountId
            amount shouldBe src.amount.toBigDecimal().movePointLeft(2)
            timestamp shouldBe src.timestamp
            counterpartyAccountRef shouldBe src.counterpartyAccountRef
        }
    }
}