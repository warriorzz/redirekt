package io.github.warriorzz.redirekt.config

import dev.schlaubi.envconf.environment
import dev.schlaubi.envconf.getEnv

object Config {

    val GITHUB_CLIENT_ID by environment
    val GITHUB_REDIRECT_URI by getEnv(default = "")
    val STYLESHEET_URL by getEnv(default = "https://raw.githubusercontent.com/sindresorhus/github-markdown-css/main/github-markdown.css")
    val PORT by getEnv(default = 8080) { it.toInt() }
    val DATABASE_NAME by getEnv(default = "redirekt")
    val DATABASE_URL by environment
    val USE_GITHUB_API by getEnv(default = false) { it.toBoolean() }
}
