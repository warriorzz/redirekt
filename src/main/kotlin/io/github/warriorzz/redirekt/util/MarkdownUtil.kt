package io.github.warriorzz.redirekt.util

import io.github.warriorzz.redirekt.config.Config
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.freemarker.*
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

object MarkdownUtil {
    private val flavour = GFMFlavourDescriptor()

    suspend fun computeMarkdown(content: String): String {
        return if (Config.USE_GITHUB_API) fetchGithubMarkdown(content) else generateMarkdown(content.replace("\r\n", "\n"))
    }

    private suspend fun fetchGithubMarkdown(content: String): String {
        return httpClient.post("https://api.github.com/markdown") {
            header("Accept", "application/vnd.github.v3+json")
            body = "{\"text\":\"${content.replace("\r\n", "\n")}\"}"
        }
    }

    private fun generateMarkdown(content: String): String {
        return HtmlGenerator(content, MarkdownParser(flavour).buildMarkdownTreeFromString(content), flavour).generateHtml()
    }

    private val httpClient = HttpClient(CIO)
}

suspend fun ApplicationCall.respondMarkdown(content: String, title: String = "Redirekt") {
    respondTemplate("markdown.ftl", mapOf("content" to content, "styleSheet" to Config.SERVER_URL + "/static/style.css", "title" to title, "icon" to Config.SERVER_URL + "/static/favicon.png"))
}
