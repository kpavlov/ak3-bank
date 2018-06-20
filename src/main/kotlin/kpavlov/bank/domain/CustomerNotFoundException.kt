package kpavlov.bank.domain

class CustomerNotFoundException(private val msg: String) : RuntimeException(msg)