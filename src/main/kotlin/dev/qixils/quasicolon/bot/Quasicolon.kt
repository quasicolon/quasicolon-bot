package dev.qixils.quasicolon.bot

import dev.qixils.quasicord.Quasicord
import net.dv8tion.jda.api.entities.Activity
import java.util.Locale
import kotlin.io.path.Path

const val namespace: String = "quasicolon"

fun main(args: Array<String>) {
    Quasicolon.jda.awaitShutdown()
}

object Quasicolon : Quasicord(
    namespace,
    listOf(Locale.ENGLISH),
    Path("."),
    Activity.listening("/help"),
    null
) {
}