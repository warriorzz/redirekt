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
        val lines = this::class.java.classLoader.getResourceAsStream("locales/Strings_$locale.properties")?.bufferedReader(Charsets.UTF_8)
            ?.readLines() ?: throw java.lang.RuntimeException("No valid locale specified.")
        lines.map { it.split("=") }.forEach { list[it[0]] = it[1] }
    }
}

fun translate(key: String, list: List<String>? = null): String = Localization.list[key]?.edit {
    var replaced = this
    list?.indices?.forEach { index ->
        replaced = replaced.replace("{$index}", list[index])
    }
    return@edit replaced
} ?: throw java.lang.RuntimeException("Unknown key for localization: $key")

private fun <A> A.edit(builder: (A.() -> A)): A {
    return this.builder()
}

suspend fun ApplicationCall.respondLocalizedTemplate(name: String, map: Map<String, String>) {
    respondTemplate(name, buildMap<String, String> {
        Localization.list.forEach {
            this[it.key.replace(".", "_")] = it.value
        }
        map.forEach {
            this[it.key] = it.value
        }
    })
}
