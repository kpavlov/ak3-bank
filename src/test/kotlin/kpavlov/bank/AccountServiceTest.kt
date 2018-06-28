package kpavlov.bank

import com.devskiller.jfairy.Fairy
import com.devskiller.jfairy.producer.person.Person
import io.kotlintest.matchers.date.shouldNotBeBefore
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.runBlocking
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import kpavlov.bank.domain.AccountType
import kpavlov.bank.domain.Customer
import kpavlov.bank.domain.CustomerDetails
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
    private val fairy = Fairy.create()
    private lateinit var person: Person

    private var customerId: CustomerId = -1

    @Before
    fun before() {
        startTime = clock.instant().atOffset(ZoneOffset.UTC)

        person = fairy.person()

        runBlocking {
            customersApi.createCustomer(Customer(person.firstName, person.lastName))
                    .thenAccept {
                        customerId = it.id
                    }.await()

        }

    }

    @Test
    fun shouldGetCustomerDetails() {

        val customerDetails = getCustomerDetails(customerId)


        verifyCustomerInfo(customerDetails).also {
            it.balance shouldBe 0
            it.accounts.size shouldBe 0
        }
    }

    private fun verifyCustomerInfo(customerDetails: CustomerDetails): CustomerDetails {
        with(customerDetails) {
            shouldNotBe(null)
            id shouldBe customerId
            firstName shouldBe person.firstName
            lastName shouldBe person.lastName
        }
        return customerDetails
    }

    @Test
    fun shouldOpenAccountWithBalance() {
        //given
        val initialCreditCents = (1..1000_000_00).random()
        val initialCredit = BigDecimal(initialCreditCents).movePointLeft(2)
        //when
        val createdAccountEvt = runBlocking {
            accountsApi.openAccount(customerId, initialCredit = initialCredit)
        }.toCompletableFuture().get()

        //then
        createdAccountEvt shouldNotBe null
        createdAccountEvt.accountId shouldNotBe null
        createdAccountEvt.customerBalanceCents shouldBe initialCreditCents

        //and also
        val customerDetails = getCustomerDetails(customerId)

        verifyCustomerInfo(customerDetails).also {
            with(it) {
                balance shouldBe initialCreditCents
                accounts.size shouldBe 1
                with(accounts[0]) {
                    id shouldBe createdAccountEvt.accountId
                    type shouldBe AccountType.CURRENT
                    timestamp.shouldNotBeBefore(startTime)
                    transactions.size shouldBe 1
                    transactions[0].amount shouldBe initialCreditCents
                }
            }

        }
    }

    private fun getCustomerDetails(customerId: CustomerId): CustomerDetails {
        return runBlocking {
            customersApi.getCustomerDetails(customerId).await()
        }
    }
}
