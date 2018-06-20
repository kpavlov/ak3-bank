package kpavlov.bank

import akka.actor.ActorSystem
import akka.event.EventStream
import akka.event.Logging
import akka.testkit.javadsl.TestKit
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.runBlocking
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import kpavlov.bank.api.model.CustomerDetails
import kpavlov.bank.domain.CustomerId
import kpavlov.bank.services.KoinModule
import kpavlov.bank.services.actors.CustomerBalanceUpdatedEvt
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.koin.standalone.StandAloneContext.closeKoin
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.koin.test.dryRun
import scala.concurrent.duration.Duration
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

const val tyrionId = "1"
const val bronnId = "2"

class AccountServiceTest : KoinTest {

    companion object {
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

    val accountsApi: AccountsApi by inject()
    val customersApi: CustomersApi by inject()
    val eventStream: EventStream by inject()
    val actorSystem: ActorSystem by inject()

    lateinit var probe: TestKit

    @Before
    fun before() {
        probe = TestKit(actorSystem)
        eventStream.setLogLevel(Logging.DebugLevel())
    }

    @Test
    fun shouldStart() {
        dryRun()

        val tyrion = getCustomerDetails(tyrionId)
        with(tyrion) {
            shouldNotBe(null)
            id.shouldBe(tyrionId)
            firstName.shouldBe("Tirion")
            lastName.shouldBe("Lannister")
            balance.longValueExact().shouldBe(0)
        }

        val bronn = getCustomerDetails(bronnId)
        with(bronn) {
            shouldNotBe(null)
            id.shouldBe(bronnId)
            firstName.shouldBe("Bronn")
            lastName.shouldBe("of the Blackwater")
            balance.longValueExact().shouldBe(0)
        }
    }

    @Test
    fun shouldOpenAccount() {
        println("AccountServiceTest.shouldOpenAccount")

        val accountId = accountsApi.openAccount(tyrionId, BigDecimal.ONE)
                .toCompletableFuture().get()

        accountId.shouldNotBe(null)
        val msg = probe.expectNoMsg(Duration.create(100, TimeUnit.MILLISECONDS))

        val balanceUpdatedEvt = probe.expectMsgClass(CustomerBalanceUpdatedEvt::class.java)
//        probe.expectMsg(java.time.Duration.ofMillis(1000), "hello");


        val receiveN = probe.receiveN(100)

        val customerDetails = getCustomerDetails(tyrionId)

        println(customerDetails)
        customerDetails.balance.longValueExact().shouldBe(100)
    }

    private fun getCustomerDetails(customerId: CustomerId): CustomerDetails {
        return runBlocking {
            customersApi.getCustomerDetails(customerId).await()
        }
    }
}
