package io.github.warriorzz.redirekt

import io.github.warriorzz.redirekt.config.Config
import io.github.warriorzz.redirekt.io.redirektModule
import io.github.warriorzz.redirekt.server.RedirektServer
import io.github.warriorzz.redirekt.util.Localization
import io.github.warriorzz.redirekt.util.translate
import mu.KotlinLogging
import org.litote.kmongo.serialization.registerModule
import java.io.File

private val logger = KotlinLogging.logger {}

fun main() {
    Localization()
    logger.info { translate("log.start") }
    registerModule(redirektModule)
    File(Config.FILE_ROOT_DIRECTORY).mkdirs()
    logger.info { translate("log.server.start") }
    RedirektServer()
    logger.info { translate("log.server.stop") }
}
