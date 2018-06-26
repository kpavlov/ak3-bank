package kpavlov.bank.tests

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.restassured.RestAssured
import kpavlov.bank.rest.mainModule
import kpavlov.bank.services.KoinModule
import org.junit.AfterClass
import org.junit.BeforeClass
import org.koin.standalone.StandAloneContext
import java.util.concurrent.TimeUnit

abstract class AbstractIT {

    companion object {

        private var server: NettyApplicationEngine? = null

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            val port = 8080

            StandAloneContext.startKoin(listOf(KoinModule))

            server = embeddedServer(Netty, port = port, module = Application::mainModule)
                    .start(false)

            RestAssured.baseURI = "http://localhost:$port"
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        }

        @JvmStatic
        @AfterClass
        fun afterClass() {
            server?.stop(10, 100, TimeUnit.MILLISECONDS)
        }
    }


}
