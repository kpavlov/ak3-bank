package kpavlov.bank

import io.kotlintest.matchers.date.shouldNotBeBefore
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.runBlocking
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import kpavlov.bank.api.model.CustomerDetails
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.services.KoinModule
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.koin.standalone.StandAloneContext.closeKoin
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.koin.test.dryRun
import java.math.BigDecimal
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset


class AccountServiceTest : KoinTest {

    companion object {

        private val clock = Clock.systemUTC()

        @BeforeClass
        @JvmStatic
        fun beforeClassAll() {
            startKoin(listOf(KoinModule))
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            closeKoin()
        }
    }

    private val accountsApi: AccountsApi by inject()
    private val customersApi: CustomersApi by inject()
    private lateinit var startTime: OffsetDateTime

    @Before
    fun before() {
        startTime = clock.instant().atOffset(ZoneOffset.UTC)
    }

    @Test
    fun shouldStart() {
        dryRun()

        val bronn = getCustomerDetails(bronnId)
        with(bronn) {
            shouldNotBe(null)
            id.shouldBe(bronnId)
            firstName.shouldBe("Bronn")
            lastName.shouldBe("of the Blackwater")
            balance.longValueExact().shouldBe(0)
            accounts.size shouldBe 0
        }
    }

    @Test
    fun shouldOpenAccount() {

        val initialCreditCents = (1..1000_000_00).random()
        val initialCredit = BigDecimal(initialCreditCents).movePointLeft(2)
        val createdAccountEvt = accountsApi.openAccount(tyrionId, initialCredit)
                .toCompletableFuture().get()

        createdAccountEvt shouldNotBe null
        createdAccountEvt.accountId shouldNotBe null
        createdAccountEvt.customerBalanceCents shouldBe initialCreditCents

        val tyrion = getCustomerDetails(tyrionId)
        with(tyrion) {
            shouldNotBe(null)
            id.shouldBe(tyrionId)
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

    private fun getCustomerDetails(customerId: CustomerId): CustomerDetails {
        return runBlocking {
            customersApi.getCustomerDetails(customerId).await()
        }
    }
}
