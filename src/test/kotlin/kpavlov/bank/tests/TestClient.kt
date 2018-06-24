package kpavlov.bank.tests

import io.restassured.RestAssured
import io.restassured.http.ContentType
import kpavlov.bank.api.model.AccountStatement
import kpavlov.bank.api.model.CustomerDetails
import kpavlov.bank.domain.AccountType
import kpavlov.bank.rest.CreateAccountRequest
import java.math.BigDecimal

object TestClient {


    init {
        RestAssured.baseURI = System.getProperty("sut.url", "http://localhost:8080")
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    fun getCustomerDetails(customerId: Int): CustomerDetails {
        return RestAssured
                .given()
                .log().uri()
                .get("/customers/{customerId}", customerId)
                .then()
                .log().body()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().`as`(CustomerDetails::class.java)
    }

    fun getAccountStatement(customerId: Int, accountId: Int): AccountStatement {
        return RestAssured
                .given()
                .log().uri()
                .get("/customers/{customerId}/accounts/{accountId}", customerId, accountId)
                .then()
                .log().body()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().`as`(AccountStatement::class.java)
    }

    fun createAccount(customerId: Int, initialCredit: BigDecimal? = null, type: AccountType? = null): Int {
        val requestSpecification = RestAssured
                .given()

        val req = CreateAccountRequest(initialCredit = initialCredit ?: BigDecimal.ZERO,
                type = type ?: AccountType.CURRENT)

        val location = requestSpecification
                .log().uri()
                .log().body()
                .body(req)
                .contentType(ContentType.JSON)
                .post("/customers/{customerId}/accounts", customerId)
                .then()
                .log().headers()
                .statusCode(201)
                .extract().header(io.ktor.http.HttpHeaders.Location)

        return Integer.parseInt(location.takeLastWhile { ch -> ch != '/' })
    }

}