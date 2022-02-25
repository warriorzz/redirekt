package io.github.warriorzz.redirekt.config

import dev.schlaubi.envconf.environment
import dev.schlaubi.envconf.getEnv

object Config {

    val AUTHORIZED_GITHUB_USER by environment
    val GITHUB_CLIENT_ID by environment
    val GITHUB_CLIENT_SECRET by environment
    val SERVER_URL by environment
    val DASHBOARD_URL by getEnv(default = SERVER_URL)
    val DASHBOARD_MODE by getEnv(default = true) { it.toBoolean() }
    val PORT by getEnv(default = 8088) { it.toInt() }
    val DASHBOARD_PORT by getEnv(default = 8089) { it.toInt() }
    val DATABASE_NAME by getEnv(default = "redirekt")
    val DATABASE_URL by environment
    val USE_GITHUB_API by getEnv(default = false) { it.toBoolean() }
    val FILE_ROOT_DIRECTORY by getEnv(default = "/usr/app/files")
    val LOCALE by getEnv(default = "en")
}
