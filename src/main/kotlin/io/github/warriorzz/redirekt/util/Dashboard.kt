package io.github.warriorzz.redirekt.util

import io.github.warriorzz.redirekt.config.Config
import io.github.warriorzz.redirekt.io.*
import io.github.warriorzz.redirekt.model.GitHubUserResponse
import io.github.warriorzz.redirekt.server.RedirektServer
import io.github.warriorzz.redirekt.server.UserSession
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litote.kmongo.eq
import java.io.File
import java.util.*

fun Routing.configureDashboard() {
    get("/") {
        RedirektServer.logger.debug { translate("log.request.main") }
        call.respondLocalizedTemplate(
            "main.ftl", mapOf(
                "loginUrl" to Config.DASHBOARD_URL + "/login",
                "styleSheet" to Config.SERVER_URL + "/static/dashboard.css",
                "icon" to Config.SERVER_URL + "/static/favicon.png",
            )
        )
    }

    authenticate("auth-dashboard") {
        get("/login") {
            RedirektServer.logger.debug { translate("log.request.login") }
            if (call.sessions.get<UserSession>() != null) {
                call.respondRedirect(Config.DASHBOARD_URL + "/dashboard")
                RedirektServer.logger.info { translate("log.login.success.session") }
                return@get
            }

            val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()

            if (principal == null) {
                call.respondMarkdown(MarkdownUtil.computeMarkdown("# Error"), "Redirekt - Error")
                RedirektServer.logger.info { translate("log.login.fail.principal") }
                return@get
            }

            val login = RedirektServer.httpClient.get<GitHubUserResponse>("https://api.github.com/user") {
                header("Authorization", "token ${principal.accessToken}")
                header("Accept", "application/vnd.github.v3+json")
            }.login

            if (login != Config.AUTHORIZED_GITHUB_USER) {
                call.respondMarkdown(MarkdownUtil.computeMarkdown("# Access denied"), "Redirekt - Access denied")
                RedirektServer.logger.info { translate("log.login.fail.user", listOf(login)) }
                return@get
            }

            RedirektServer.logger.info { translate("log.login.success.user", listOf(login)) }
            call.sessions.set(UserSession(principal.accessToken))
            call.respondRedirect(Config.DASHBOARD_URL + "/dashboard")
        }

        route("/dashboard") {
            get {
                RedirektServer.logger.debug { translate("log.request.dashboard")}
                if (call.sessions.get<UserSession>() == null) {
                    call.respondRedirect(Config.DASHBOARD_URL + "/login")
                    RedirektServer.logger.info { translate("log.dashboard.fail.session") }
                    return@get
                }
                RedirektServer.logger.info { translate("log.dashboard.success") }
                call.respondLocalizedTemplate(
                    "dashboard.ftl",
                    mapOf(
                        "styleSheet" to Config.SERVER_URL + "/static/dashboard.css",
                        "errorBanner" to if ((call.parameters["error"] ?: "false").toBoolean())
                            """<div class="banner">
                                <p>${translate("dashboard.error")}</p>
                            </div>
                            """ else "",
                        "successBanner" to if ((call.parameters["success"] ?: "false").toBoolean())
                            """<div class="banner">
                                <p>${translate("dashboard.success")}</p>
                            </div>
                            """ else "",
                        "icon" to Config.SERVER_URL + "/static/favicon.png"
                    )
                )
            }

            post("/markdown") {
                RedirektServer.logger.info { translate("log.dashboard.submit.markdown.attempt") }
                if (call.sessions.get<UserSession>() == null) {
                    RedirektServer.logger.info { translate("log.dashboard.submit.markdown.fail.session") }
                    return@post
                }

                val multipart = call.receiveMultipart()

                var markdownText = ""
                var name = ""

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && part.contentType != ContentType("text", "markdown")) {
                        markdownText = String(withContext(Dispatchers.IO) {
                            part.streamProvider().readAllBytes()
                        })
                    }
                    if (part is PartData.FormItem) {
                        withContext(Dispatchers.IO) {
                            name = part.value
                        }
                    }
                    part.dispose()
                }

                if (Repositories.entries.findOne(RedirektEntry::name eq name) != null) {
                    call.respondRedirect("/dashboard?error=true")
                    RedirektServer.logger.info { translate("log.dashboard.submit.markdown.fail.doubled", listOf(name)) }
                    return@post
                }

                RedirektServer.logger.info { translate("log.dashboard.submit.markdown.new", listOf(name)) }

                Repositories.entries.insertOne(
                    RedirektEntry(
                        name,
                        MarkdownEntry(MarkdownUtil.computeMarkdown(markdownText))
                    )
                )

                call.respondRedirect("${Config.DASHBOARD_URL}/dashboard?success=true")
            }

            post("/file") {
                RedirektServer.logger.info { translate("log.dashboard.submit.file.attempt") }
                if (call.sessions.get<UserSession>() == null) {
                    RedirektServer.logger.info { translate("log.dashboard.submit.file.fail.session") }
                    return@post
                }

                val multipart = call.receiveMultipart()

                var name = ""
                var path = ""
                val fileUuid = UUID.randomUUID().toString()

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && part.contentType != ContentType("text", "markdown")) {
                        withContext(Dispatchers.IO) {
                            path = "${Config.FILE_ROOT_DIRECTORY}/$fileUuid.${
                                part.originalFileName?.split(".")?.get(1) ?: ""
                            }"
                            part.streamProvider().transferTo(File(path).outputStream())
                        }
                    }
                    if (part is PartData.FormItem) {
                        withContext(Dispatchers.IO) {
                            name = part.value
                        }
                    }
                    part.dispose()
                }

                if (Repositories.entries.findOne(RedirektEntry::name eq name) != null) {
                    RedirektServer.logger.info { translate("log.dashboard.submit.file.fail.doubled", listOf(name)) }
                    call.respondRedirect("/dashboard?error=true")
                    return@post
                }

                RedirektServer.logger.info { translate("log.dashboard.submit.file.new", listOf(name)) }

                Repositories.entries.insertOne(
                    RedirektEntry(
                        name,
                        FileEntry(path)
                    )
                )

                call.respondRedirect("${Config.DASHBOARD_URL}/dashboard?success=true")
            }

            post("/redirect") {
                RedirektServer.logger.info { translate("log.dashboard.submit.redirekt.attempt") }
                if (call.sessions.get<UserSession>() == null) {
                    RedirektServer.logger.info { translate("log.dashboard.submit.redirekt.fail.session") }
                    return@post
                }

                val parameters = call.receiveParameters()
                val name = parameters["name"]
                val redirekt = parameters["value"]

                if (name == null || redirekt == null) {
                    RedirektServer.logger.info { translate("log.dashboard.submit.redirekt.fail.invalid") }
                    call.respondText("error")
                    return@post
                }

                if (Repositories.entries.findOne(RedirektEntry::name eq name) != null) {
                    RedirektServer.logger.info { translate("log.dashboard.submit.redirekt.fail.doubled", listOf(name)) }
                    call.respondRedirect("/dashboard?error=true")
                    return@post
                }

                RedirektServer.logger.info { translate("log.dashboard.submit.redirekt.new", listOf(name)) }

                Repositories.entries.insertOne(RedirektEntry(name, RedirectEntry(redirekt)))
                call.respondRedirect("${Config.SERVER_URL}/dashboard?success=true")
            }
        }
    }
}
