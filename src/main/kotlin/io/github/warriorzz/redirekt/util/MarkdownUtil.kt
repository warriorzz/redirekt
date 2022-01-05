package io.github.warriorzz.redirekt.util

import io.github.warriorzz.redirekt.config.Config
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.freemarker.*
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

object MarkdownUtil {
    private val flavour = CommonMarkFlavourDescriptor()

    suspend fun computeMarkdown(content: String): String {
        return if (Config.USE_GITHUB_API) fetchGithubMarkdown(content) else generateMarkdown(content)
    }

    private suspend fun fetchGithubMarkdown(content: String): String {
        return ktorClient.post("https://api.github.com/markdown") {
            header("Accept", "application/json")
            body = "{\"text\": \"$content\"}"
        }
    }

    private fun generateMarkdown(content: String): String {
        return HtmlGenerator(content, MarkdownParser(flavour).buildMarkdownTreeFromString(content), flavour).generateHtml()
    }

    private val ktorClient = HttpClient(CIO)
}

suspend fun ApplicationCall.respondMarkdown(content: String) {
    respondTemplate("markdown.ftl", mapOf("content" to content, "styleSheet" to Config.STYLESHEET_URL))
}
