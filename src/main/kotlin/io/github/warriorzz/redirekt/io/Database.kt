package io.github.warriorzz.redirekt.io

import io.github.warriorzz.redirekt.config.Config
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object Repositories {
    private val client = KMongo.createClient(Config.DATABASE_URL).coroutine
    private val database = client.getDatabase(Config.DATABASE_NAME)
    val entries = database.getCollection<RedirektEntry>()
    val uploadedFiles = database.getCollection<UploadedFile>()
}

@Serializable
data class RedirektEntry(
    val name: String,
    @Polymorphic val value: RedirektEntryValue
)

@Serializable
sealed class RedirektEntryValue

@Serializable
data class MarkdownEntry(
    val markdown: String,
) : RedirektEntryValue()

@Serializable
data class RedirectEntry(
    val url: String,
) : RedirektEntryValue()

@Serializable
data class FileEntry(
    val path: String,
) : RedirektEntryValue()

@Serializable
data class UploadedFile(
    val uuid: String,
    val path: String,
)

val redirektModule = SerializersModule {
    polymorphic(RedirektEntryValue::class) {
        subclass(MarkdownEntry::class, MarkdownEntry.serializer())
        subclass(RedirectEntry::class, RedirectEntry.serializer())
        subclass(FileEntry::class, FileEntry.serializer())
    }
}
