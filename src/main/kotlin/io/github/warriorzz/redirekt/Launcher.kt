package io.github.warriorzz.redirekt

import io.github.warriorzz.redirekt.config.Config
import io.github.warriorzz.redirekt.io.redirektModule
import io.github.warriorzz.redirekt.server.RedirektServer
import org.litote.kmongo.serialization.registerModule
import java.io.File

fun main() {
    registerModule(redirektModule)
    File(Config.FILE_ROOT_DIRECTORY).mkdirs()
    RedirektServer()
}
