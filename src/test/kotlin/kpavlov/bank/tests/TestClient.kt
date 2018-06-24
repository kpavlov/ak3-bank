package kpavlov.bank.tests

import io.restassured.RestAssured
import io.restassured.http.ContentType
import kpavlov.bank.api.model.CustomerDetails
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
                .get("/customers/{id}", customerId)
                .then()
                .log().body()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().`as`(CustomerDetails::class.java)
    }

    fun createAccount(customerId: Int, initialCredit: BigDecimal?): Int {
        val requestSpecification = RestAssured
                .given()

        if (initialCredit != null) {
            requestSpecification.queryParam("initialCredit", initialCredit)
        }
        val location = requestSpecification
                .log().uri()
                .log().parameters()
                .post("/customers/{id}/accounts", customerId)
                .then()
                .log().headers()
                .statusCode(201)
                .extract().header(io.ktor.http.HttpHeaders.Location)

        return Integer.parseInt(location.takeLastWhile { ch -> ch != '/' })
    }

}