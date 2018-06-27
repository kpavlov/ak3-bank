package kpavlov.bank.services

import kpavlov.bank.domain.Customer

class Bootstrap(customersService: CustomersService) {

    init {
        with(customersService) {
            createCustomer(Customer(firstName = "Tirion", lastName = "Lannister"))
            createCustomer(Customer(firstName = "Bronn", lastName = "of the Blackwater"))
        }
    }

}