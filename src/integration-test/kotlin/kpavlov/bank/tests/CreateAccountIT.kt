package kpavlov.bank.tests

import io.kotlintest.matchers.date.shouldNotBeBefore
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import kpavlov.bank.random
import kpavlov.bank.rest.v1.model.AccountType
import kpavlov.bank.rest.v1.model.CustomerId
import org.hamcrest.CoreMatchers.equalTo
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

    companion object {
        private var customerId: CustomerId = -1
    }


    @Before
    fun before() {
        startTime = Clock.systemUTC().instant().atOffset(ZoneOffset.UTC)
    }

    @Test
    fun test0_shouldCreateAccount() {
        // when
        val customerDetails = TestClient.createCustomer("Tirion", "Lannister")

        customerDetails shouldNotBe null
        customerId = customerDetails.id

        // then
        with(customerDetails) {
            shouldNotBe(null)
            firstName shouldBe "Tirion"
            lastName shouldBe "Lannister"
            balance.compareTo(BigDecimal.ZERO) shouldBe 0
            accounts.size shouldBe 0
        }
    }

    @Test
    fun test1_shouldGetAccountDetailsBeforeUpdate() {
        // when
        val customerDetails = TestClient.getCustomerDetails(customerId)

        // then
        with(customerDetails) {
            shouldNotBe(null)
            id.shouldBe(customerId)
            firstName.shouldBe("Tirion")
            lastName.shouldBe("Lannister")
            balance.compareTo(BigDecimal.ZERO) shouldBe 0
            accounts.size shouldBe 0
        }
    }

    @Test
    fun test2_shouldCreateAccountWithNoBalance() {

        // when
        val accountId = TestClient.createAccount(customerId)

        // then
        accountId shouldBe 1

        val customerDetails = TestClient.getCustomerDetails(customerId)
        with(customerDetails) {
            shouldNotBe(null)
            id.shouldBe(customerId)
            firstName.shouldBe("Tirion")
            lastName.shouldBe("Lannister")
            balance.compareTo(BigDecimal.ZERO) shouldBe 0
            accounts.size shouldBe 1
            with(accounts[0]) {
                id.shouldBeGreaterThan(0)
                type shouldBe AccountType.CURRENT
                timestamp.shouldNotBeBefore(startTime)
                transactions.size shouldBe 0
            }
        }
    }

    @Test
    fun test3_shouldCreateSavingsAccountWithInitialAmount() {
        val initialCreditCents = (1..1000_000_00).random()
        val initialCredit = BigDecimal(initialCreditCents).movePointLeft(2)

        // when
        val accountId = TestClient.createAccount(customerId, initialCredit, AccountType.SAVINGS)

        // then
        accountId shouldBe 2

        val customerDetails = TestClient.getCustomerDetails(customerId)
        with(customerDetails) {
            shouldNotBe(null)
            id.shouldBe(customerId)
            firstName.shouldBe("Tirion")
            lastName.shouldBe("Lannister")
            balance.compareTo(initialCredit) shouldBe 0
            accounts.size shouldBe 2
            with(accounts[1]) {
                id.shouldBeGreaterThan(0)
                type shouldBe AccountType.SAVINGS
                timestamp.shouldNotBeBefore(startTime)
                transactions.size shouldBe 1
                transactions[0].amount shouldBe initialCredit
            }
        }
    }

    @Test
    fun test4_shouldGetAccountStatement() {
        //given
        val accountId1 = 2
        // when
        val accountStatement = TestClient.getAccountStatement(customerId, accountId1)

        // then
        with(accountStatement) {
            shouldNotBe(null)
            id shouldBe accountId1
            type shouldBe AccountType.SAVINGS
            timestamp.shouldNotBeBefore(startTime)
            transactions.size shouldBe 1
            transactions[0].amount.signum() shouldBe 1
        }
    }

    @Test
    fun test5_shouldGet404WhenCustomerNotFound() {
        verify404Response("/customers/{customerId}", mapOf(Pair("customerId", 999)))
    }

    @Test
    fun test6_shouldGet404WhenAccountNotFound() {
        verify404Response("/customers/{customerId}/accounts/{accountId}", mapOf(
                Pair("customerId", customerId), Pair("accountId", 999)))
    }

    private fun verify404Response(path: String, params: Map<String, *>) {
        RestAssured
                .given()
                .log().uri()
                .get(path, params)
                .then()
                .assertThat()
                .log().all()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("status", equalTo(404))
                .body("title", equalTo("Not Found"))
    }


}