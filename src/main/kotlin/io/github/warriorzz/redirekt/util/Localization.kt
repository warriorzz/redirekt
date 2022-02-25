package io.github.warriorzz.redirekt.util

import io.github.warriorzz.redirekt.config.Config

object Localization {

    val list = HashMap<String, String>()

    operator fun invoke() {
        loadLanguageProperties()
    }

    private fun loadLanguageProperties() {
        val lines = this::class.java.classLoader.getResourceAsStream("locales/Strings_${Config.LOCALE}.properties")?.bufferedReader()
            ?.readLines() ?: throw java.lang.RuntimeException("No valid locale specified.")
        lines.map { it.split("=") }.forEach { list[it[0]] = it[1] }
    }
}

fun translate(key: String): String = Localization.list[key] ?: throw java.lang.RuntimeException("Unknown key for localization: $key")
