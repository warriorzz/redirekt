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
                val entry = Repositories.entries.findOne(RedirektEntry::name eq name)
                if (entry != null) {
                    if (entry.value is RedirectEntry) {
                        call.respondRedirect(entry.value.url)
                    } else if (entry.value is MarkdownEntry) {
                        call.respondMarkdown(entry.value.markdown)
                    } else if (entry.value is FileEntry) {
                        call.respondFile(File(entry.value.path)) {}
                    } else {
                        logger.error { translate("log.redirekt.invalidformat", listOf(name ?: "")) }
                        call.respondMarkdown(
                            MarkdownUtil.computeMarkdown("# ${translate("respond.unsupported")}"),
                            "Redirekt"
                        )
                    }
                } else {
                    logger.info { translate("log.redirekt.error", listOf(name ?: "")) }
                    call.respondMarkdown(
                        MarkdownUtil.computeMarkdown("# ${translate("respond.error")}"),
                        "Redirekt - ${translate("respond.error")}"
                    )
                }
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
