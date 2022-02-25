package io.github.warriorzz.redirekt.util

import io.github.warriorzz.redirekt.config.Config
import io.ktor.application.*
import io.ktor.freemarker.*

object Localization {

    val list = HashMap<String, String>()

    operator fun invoke() {
        loadLanguageProperties("en")
        loadLanguageProperties(Config.LOCALE)
    }

    private fun loadLanguageProperties(locale: String) {
        val lines = this::class.java.classLoader.getResourceAsStream("locales/Strings_${locale}.properties")?.bufferedReader()
            ?.readLines() ?: throw java.lang.RuntimeException("No valid locale specified.")
        lines.map { it.split("=") }.forEach { list[it[0]] = it[1] }
    }
}

fun translate(key: String): String = Localization.list[key] ?: throw java.lang.RuntimeException("Unknown key for localization: $key")

suspend fun ApplicationCall.respondLocalizedTemplate(name: String, map: Map<String, String>) {
    respondTemplate(name, buildMap<String, String> {
        map.forEach {
            this[it.key] = it.value
        }
        Localization.list.forEach {
            this[it.key.replace(".", "_")] = it.value
        }
    })
}
