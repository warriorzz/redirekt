package io.github.warriorzz.redirekt.util

import io.github.warriorzz.redirekt.config.Config
import io.github.warriorzz.redirekt.io.*
import io.github.warriorzz.redirekt.model.GitHubUserResponse
import io.github.warriorzz.redirekt.model.v1.FileRequest
import io.github.warriorzz.redirekt.model.v1.RedirectRequest
import io.github.warriorzz.redirekt.server.RedirektServer
import io.github.warriorzz.redirekt.server.UserSession
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.coroutines.*
import org.litote.kmongo.eq
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

private val discardUploadFilesScope = CoroutineScope(Dispatchers.IO + Job())

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
                call.respondMarkdown(
                    MarkdownUtil.computeMarkdown("# ${translate("respond.error")}"),
                    "Redirekt - Error"
                )
                RedirektServer.logger.info { translate("log.login.fail.principal") }
                return@get
            }

            val login = RedirektServer.httpClient.get<GitHubUserResponse>("https://api.github.com/user") {
                header("Authorization", "token ${principal.accessToken}")
                header("Accept", "application/vnd.github.v3+json")
            }.login

            if (login != Config.AUTHORIZED_GITHUB_USER) {
                call.respondMarkdown(
                    MarkdownUtil.computeMarkdown("# Access denied"),
                    "Redirekt - ${translate("respond.accessdenied")}"
                )
                RedirektServer.logger.info { translate("log.login.fail.user", listOf(login)) }
                return@get
            }

            RedirektServer.logger.info { translate("log.login.success.user", listOf(login)) }
            call.sessions.set(UserSession(principal.accessToken))
            call.respondRedirect(Config.DASHBOARD_URL + "/dashboard")
        }

        get("/dashboard") {
            RedirektServer.logger.debug { translate("log.request.dashboard") }
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
                    "jsSheet" to Config.SERVER_URL + "/static/dashboard.js",
                    "icon" to Config.SERVER_URL + "/static/favicon.png"
                )
            )
        }

        route("/api/v1") {
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
                    RedirektServer.logger.info {
                        translate(
                            "log.dashboard.submit.markdown.fail.doubled",
                            listOf(name)
                        )
                    }
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
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                val request = call.receive<FileRequest>()

                if (Repositories.entries.findOne(RedirektEntry::name eq request.name) != null) {
                    RedirektServer.logger.info {
                        translate(
                            "log.dashboard.submit.file.fail.doubled",
                            listOf(request.name)
                        )
                    }
                    call.respondText(status = HttpStatusCode.Conflict) { "Error - duplicated" }
                    return@post
                }

                val uploadedFile = Repositories.uploadedFiles.find(UploadedFile::uuid eq request.uuid).first()
                if (uploadedFile == null) {
                    RedirektServer.logger.info {
                        translate(
                            "log.dashboard.submit.file.fail.invalid",
                            listOf(request.name)
                        )
                    }
                    call.respondText(status = HttpStatusCode.Conflict) { "Error - file not found" }
                    return@post
                }

                RedirektServer.logger.info { translate("log.dashboard.submit.file.new", listOf(request.name)) }

                Repositories.entries.insertOne(
                    RedirektEntry(
                        request.name,
                        FileEntry(uploadedFile.path)
                    )
                )
                Repositories.uploadedFiles.deleteOne(UploadedFile::uuid eq uploadedFile.uuid)
                call.respondText(status = HttpStatusCode.Accepted) { "Success" }
            }

            post("/redirect") {
                RedirektServer.logger.info { translate("log.dashboard.submit.redirekt.attempt") }
                if (call.sessions.get<UserSession>() == null) {
                    RedirektServer.logger.info { translate("log.dashboard.submit.redirekt.fail.session") }
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                val request = call.receive<RedirectRequest>()

                if (Repositories.entries.findOne(RedirektEntry::name eq request.name) != null) {
                    RedirektServer.logger.info {
                        translate(
                            "log.dashboard.submit.redirekt.fail.doubled",
                            listOf(request.name)
                        )
                    }
                    call.respondText(status = HttpStatusCode.Conflict) { "Error - duplicated" }
                    return@post
                }

                RedirektServer.logger.info { translate("log.dashboard.submit.redirekt.new", listOf(request.name)) }

                Repositories.entries.insertOne(RedirektEntry(request.name, RedirectEntry(request.value)))
                call.respondText(status = HttpStatusCode.Accepted) { "Success" }
            }

            post("/upload") {
                var path = ""
                val fileUuid = UUID.randomUUID().toString()

                val parts = call.receiveMultipart().readAllParts()

                if (parts.size != 1 && parts[0] is PartData.FileItem && parts[0].contentType != ContentType(
                        "text",
                        "markdown"
                    )
                ) {
                    call.respond(HttpStatusCode.BadRequest)
                }

                parts[0].let { part ->
                    part as PartData.FileItem
                    withContext(Dispatchers.IO) {
                        path = "${Config.FILE_ROOT_DIRECTORY}/$fileUuid.${
                            part.originalFileName?.split(".")?.get(1) ?: ""
                        }"
                        part.streamProvider().transferTo(File(path).outputStream())
                    }

                    part.dispose()
                }

                Repositories.uploadedFiles.insertOne(UploadedFile(fileUuid, path))
                discardUploadFilesScope.launch {
                    delay(30 * 60 * 1000)
                    Repositories.uploadedFiles.find(UploadedFile::uuid eq fileUuid).first()?.let {
                        Repositories.uploadedFiles.deleteOne(UploadedFile::uuid eq it.uuid)
                        Files.delete(Path.of(it.path))
                    }
                }

                call.respondText(status = HttpStatusCode.Accepted) { "Success" }
            }
        }
    }
}

suspend fun <T, K> T.transform(block: suspend T.() -> K): K = this.block()
