package kpavlov.bank.tests

import io.kotlintest.matchers.date.shouldNotBeBefore
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kpavlov.bank.domain.AccountType
import kpavlov.bank.random
import kpavlov.bank.tyrionId
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.math.BigDecimal
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CreateAccountIT : AbstractIT() {

    private lateinit var startTime: OffsetDateTime

    @Before
    fun before() {
        startTime = Clock.systemUTC().instant().atOffset(ZoneOffset.UTC)
    }

    @Test
    fun test0_shouldGetAccountDetailsBeforeUpdate() {
        val customerDetails = TestClient.getCustomerDetails(tyrionId)
        with(customerDetails) {
            shouldNotBe(null)
            id.shouldBe(kpavlov.bank.tyrionId)
            firstName.shouldBe("Tirion")
            lastName.shouldBe("Lannister")
            balance.compareTo(BigDecimal.ZERO) shouldBe 0
            accounts.size shouldBe 0
        }
    }

    @Test
    fun test1_shouldCreateAccountWithInitialAmount() {
        val initialCreditCents = (1..1000_000_00).random()
        val initialCredit = BigDecimal(initialCreditCents).movePointLeft(2)

        val accountId = TestClient.createAccount(tyrionId, initialCredit)

        accountId shouldBe 1

        val customerDetails = TestClient.getCustomerDetails(tyrionId)
        with(customerDetails) {
            shouldNotBe(null)
            id.shouldBe(kpavlov.bank.tyrionId)
            firstName.shouldBe("Tirion")
            lastName.shouldBe("Lannister")
            balance.compareTo(initialCredit) shouldBe 0
            accounts.size shouldBe 1
            with(accounts[0]) {
                id.shouldBeGreaterThan(0)
                type shouldBe AccountType.CURRENT
                timestamp.shouldNotBeBefore(startTime)
                transactions.size shouldBe 1
                transactions[0].amount
            }
        }
    }

    @Test
    fun test2_shouldCreateAccountWithInitialAmount() {

    }


}