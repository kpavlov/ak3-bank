package kpavlov.bank.services.actors

import akka.actor.AbstractLoggingActor

class EventSubscriberActor(private val classes: Array<Class<Any>>) : AbstractLoggingActor() {

    override fun preStart() {
        context.system.eventStream().subscribe(self, classes)
    }

    override fun createReceive(): Receive {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}