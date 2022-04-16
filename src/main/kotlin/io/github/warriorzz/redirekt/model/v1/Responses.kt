package io.github.warriorzz.redirekt.model.v1

@kotlinx.serialization.Serializable
data class RedirectRequest(
    val name: String,
    val value: String,
)

@kotlinx.serialization.Serializable
data class FileRequest(
    val name: String,
    val uuid: String
)

@kotlinx.serialization.Serializable
data class MarkdownRequest(
    val name: String,
    val markdown: String
)