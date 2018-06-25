package kpavlov.bank.rest

import akka.pattern.AskTimeoutException
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.pipeline.PipelineContext
import io.ktor.response.defaultTextContentType
import io.ktor.response.respond
import io.ktor.routing.routing
import kpavlov.bank.api.AccountsApi
import kpavlov.bank.api.CustomersApi
import kpavlov.bank.rest.v1.model.ErrorResponse
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
                is AskTimeoutException ->
                    handleException(ResourceNotFoundException(), HttpStatusCode.NotFound)
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
    when {
        httpStatusCode.value < 500 -> log.info("Can't handle request", cause)
        else -> log.error("Error", cause)
    }
    call.defaultTextContentType(ContentType("application", "json"))
    call.respond(httpStatusCode, ErrorResponse(httpStatusCode.value, httpStatusCode.description, cause.message))
}

