# AK<sup>3</sup> Bank API

## Akka-Kotlin-Koin-Ktor (AK<sup>3</sup>) Bank API

[![Build Status](https://travis-ci.org/kpavlov/ak3-bank.svg?branch=master)](https://travis-ci.org/kpavlov/ak3-bank)

The project consists of an API to be used for opening customer bank accounts and saving transactions.

The API will expose an endpoint which accepts the user information (`customerID`, `initialCredit`).

Once the endpoint is called, a new account will be opened connected to the user whose ID is `customerID`.

Also, if `initialCredit` is not 0, a transaction will be sent to the new account.

Another Endpoint will output the user information showing _Name_, _Surname_, _balance_, and transactions of the accounts.

## Assumptions

* For simplicity, the data is saved in memory and not actually persisted, so that the solution could be easier tested.
* All accounts are opened in the same currency (which is not always a case in real life)
* Application security is skipped: no Authentication, Authorization and TLS. Full logging is enabled.
* There are no restriction for the customer to open new account. 
    The restriction could be maxActiveAccounts or max deposit amount. Now it's omitted.

## Implementation notes
* [Swagger codegen](https://github.com/swagger-api/swagger-codegen) 
  is not mature enough to generate server interfaces for Ktor server. 
  That's why only DTO classes are generated.

## Build instructions

To build with maven and start:

    ./build-and-start.sh
    

