package io.github.warriorzz.redirekt.server

import freemarker.cache.ClassTemplateLoader
import io.github.warriorzz.redirekt.config.Config
import io.github.warriorzz.redirekt.io.*
import io.github.warriorzz.redirekt.util.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.freemarker.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.litote.kmongo.eq
import java.io.File

object RedirektServer {

    val logger = KotlinLogging.logger {}
    val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }
    }

    private val server = embeddedServer(CIO, port = Config.PORT) {
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }
        if (!Config.DASHBOARD_MODE) {
            configureAuthorization()
        }
        routing {
            if (!Config.DASHBOARD_MODE) {
                configureDashboard()
            }
            get("/${if (Config.DASHBOARD_MODE) "" else "r/"}{name}") {
                val name = call.parameters["name"]
                logger.info { translate("log.request.redirekt", listOf(name ?: "")) }
                Repositories.entries.findOne(RedirektEntry::name eq name)
                    ?.let {
                        if (it.value is RedirectEntry) {
                            call.respondRedirect(it.value.url)
                        } else if (it.value is MarkdownEntry) {
                            call.respondMarkdown(it.value.markdown)
                        } else if (it.value is FileEntry) {
                            call.respondFile(File(it.value.path)) {}
                        } else {
                            logger.error { translate("log.redirekt.invalidformat") }
                            call.respondMarkdown(MarkdownUtil.computeMarkdown("# Unsupported operation"), "Redirekt")
                        }
                    } ?: call.respondMarkdown(MarkdownUtil.computeMarkdown("# Error"), "Redirekt - Error")
            }

            static("/static") {
                resources("static")
            }
        }
    }

    operator fun invoke() {
        if (Config.DASHBOARD_MODE) {
            val dashboardServer = embeddedServer(CIO, port = Config.DASHBOARD_PORT) {
                install(FreeMarker) {
                    templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
                }
                configureAuthorization()
                routing {
                    configureDashboard()
                }
            }
            dashboardServer.start(false)
        }
        server.start(wait = true)
    }
}

@Serializable
data class UserSession(val accessToken: String)
