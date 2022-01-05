package io.github.warriorzz.redirekt

import io.github.warriorzz.redirekt.io.redirektModule
import io.github.warriorzz.redirekt.server.RedirektServer
import org.litote.kmongo.serialization.registerModule

suspend fun main() {
    registerModule(redirektModule)
    RedirektServer()
}
