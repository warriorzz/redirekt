package io.github.warriorzz.redirekt.server

import freemarker.cache.ClassTemplateLoader
import io.github.warriorzz.redirekt.config.Config
import io.github.warriorzz.redirekt.io.MarkdownEntry
import io.github.warriorzz.redirekt.io.RedirectEntry
import io.github.warriorzz.redirekt.io.RedirektEntry
import io.github.warriorzz.redirekt.io.Repositories
import io.github.warriorzz.redirekt.util.MarkdownUtil
import io.github.warriorzz.redirekt.util.respondMarkdown
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.launch
import org.litote.kmongo.eq

object RedirektServer {

    private val server = embeddedServer(CIO, port = Config.PORT) {
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }
        routing {
            route("/dashboard") {
                get {
                    if (!call.parameters.contains("code")) {
                        call.respondRedirect("https://github.com/login/oauth/authorize?client_id=${Config.GITHUB_CLIENT_ID}${if (Config.GITHUB_REDIRECT_URI != "") "&redirect_uri=${Config.GITHUB_REDIRECT_URI}" else ""}")
                        return@get
                    }
                    call.respondText("Hello, world!", contentType = ContentType.Text.Plain)
                }

                get("/login") {
                    if (!call.parameters.contains("code")) {
                        call.respondRedirect("/dashboard")
                    }

                }

                post("/upload") {
                    call.request.headers["Authorization"]
                    call.respondRedirect("/dashboard")

                    val multipart = call.receiveMultipart()
                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem && part.contentType != ContentType.Text.Any && part.originalFileName?.endsWith(
                                ".md"
                            ) == true
                        ) {
                            val name = part.originalFileName!!
                            var text = ""
                            launch {
                                text = String(part.streamProvider().readAllBytes())
                            }.join()
                            Repositories.entries.insertOne(RedirektEntry(name, MarkdownEntry(text)))
                        }
                        part.dispose()
                    }
                }
            }

            get("/{name}") {
                val name = call.parameters["name"]
                Repositories.entries.findOne(RedirektEntry::name eq name)
                    ?.let {
                        if (it.value is RedirectEntry) {
                            call.respondRedirect(it.value.url)
                        } else if (it.value is MarkdownEntry) {
                            call.respondMarkdown(it.value.markdown)
                        } else {
                            call.respondRedirect("wtf.wtf")
                        }
                    } ?: call.respondRedirect("https://google.com")
            }

            static("/static") {
                resources("static")
            }
        }
    }

    suspend operator fun invoke() {
        if (Repositories.entries.find().first() == null) {
            Repositories.entries.insertOne(RedirektEntry("duckduckgo", RedirectEntry("https://duckduckgo.com")))
            Repositories.entries.insertOne(RedirektEntry("github", MarkdownEntry(MarkdownUtil.computeMarkdown("# Some content"))))
            println(MarkdownUtil.computeMarkdown("# Some content"))
        }
        server.start(wait = true)
    }
}
