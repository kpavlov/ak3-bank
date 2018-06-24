package kpavlov.bank.rest

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import io.ktor.routing.routing
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomerNotFoundException
import kpavlov.bank.api.CustomersApi
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(Application::class.java)

fun Application.mainModule() {

    install(Locations)
    install(DefaultHeaders)
    install(CallLogging)
    install(Compression) {
        gzip()
    }
    install(ContentNegotiation) {
        jackson {
            findAndRegisterModules()
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
        }
    }
    install(StatusPages) {
        exception<Throwable> { cause ->
            when (cause) {
                is CustomerNotFoundException ->
                    handleException(cause, HttpStatusCode.NotFound)
                else ->
                    handleException(cause)
            }

        }
    }

    val accountsApi by inject<AccountsApi>()
    val customersApi by inject<CustomersApi>()

    routing {
        root(accountsApi, customersApi)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleException(cause: Throwable,
                                                                           httpStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError) {
    log.error("Error", cause)
    call.respond(httpStatusCode, ErrorResponse(httpStatusCode.value, httpStatusCode.description, cause.message))
}

private data class ErrorResponse(val status: Int, val title: String, val detail: String?)

