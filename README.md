# Simple Bank API Service

[![pipeline status](https://gitlab.com/kpavlov/sample-bank/badges/master/pipeline.svg)](https://gitlab.com/kpavlov/sample-bank/commits/master)

The project consists of an API to be used for opening customer bank accounts and saving transactions.

The API will expose an endpoint which accepts the user information (`customerID`, `initialCredit`).

Once the endpoint is called, a new account will be opened connected to the user whose ID is `customerID`.

Also, if `initialCredit` is not 0, a transaction will be sent to the new account.

Another Endpoint will output the user information showing _Name_, _Surname_, _balance_, and transactions of the accounts.

For simplicity, the data is saved in memory and not actually persisted, so that the solution could be easier tested.
