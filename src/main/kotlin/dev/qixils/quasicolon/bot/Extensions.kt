package dev.qixils.quasicolon.bot

import dev.qixils.quasicord.Key
import dev.qixils.quasicord.locale.Context
import dev.qixils.quasicord.text.Text
import kotlinx.coroutines.reactor.awaitSingle
import net.dv8tion.jda.api.interactions.Interaction

fun botKey(value: String) = Key.key(namespace, value)

val Interaction.context: Context
    get() = Context.fromInteraction(this)

suspend fun Context.text(value: Key, vararg args: Any?): String =
    Text.single(value, *args).asString(this).awaitSingle()

suspend fun Context.text(value: String, vararg args: Any?): String =
    text(botKey(value), *args)