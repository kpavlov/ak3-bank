package kpavlov.bank.services.actors

import akka.actor.AbstractLoggingActor

const val ACTOR_TIMEOUT = 500L

inline fun <reified T> AbstractLoggingActor.handleActorResponse(t: Throwable?,
                                                                errorMessage: String = "Unexpected error",
                                                                evt: Any?,
                                                                clazz: Class<T>,
                                                                block: (T) -> Unit) {
    if (t != null) {
        log().error(t, "{}: {}", errorMessage, t.message)
    } else {
        when {
            clazz.isInstance(evt) -> block(evt as T)
            else -> log().warning("Unexpected message: {}", evt)
        }
    }
}