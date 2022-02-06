package io.github.warriorzz.redirekt.util

import io.github.warriorzz.redirekt.config.Config
import io.github.warriorzz.redirekt.server.RedirektServer
import io.github.warriorzz.redirekt.server.UserSession
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlin.random.Random

fun Application.configureAuthorization() {
    install(Authentication) {
        oauth("auth-dashboard") {
            urlProvider = { "${Config.DASHBOARD_URL}/login" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "github",
                    authorizeUrl = "https://github.com/login/oauth/authorize",
                    accessTokenUrl = "https://github.com/login/oauth/access_token",
                    clientId = Config.GITHUB_CLIENT_ID,
                    clientSecret = Config.GITHUB_CLIENT_SECRET,
                    authorizeUrlInterceptor = { parameters.urlEncodingOption = UrlEncodingOption.DEFAULT },
                    defaultScopes = listOf(),
                    nonceManager = RedirektNonceManager
                )
            }
            client = RedirektServer.httpClient
            skipWhen {
                it.sessions.get<UserSession>() != null
            }
        }
    }

    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60 * 60
            cookie.httpOnly = true
        }
    }
}

object RedirektNonceManager : NonceManager {

    private val list = mutableListOf<String>()
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    override suspend fun newNonce(): String {
        val nonce = (1..18)
            .map { charPool[Random.nextInt(0, charPool.size)] }
            .joinToString("")
        list.add(nonce)
        return nonce
    }

    override suspend fun verifyNonce(nonce: String): Boolean = list.remove(nonce)
}
