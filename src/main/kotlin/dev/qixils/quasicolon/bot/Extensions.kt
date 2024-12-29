package dev.qixils.quasicolon.bot

import dev.qixils.quasicord.Key
import dev.qixils.quasicord.locale.Context
import dev.qixils.quasicord.text.Text
import kotlinx.coroutines.reactor.awaitSingle
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.Interaction

fun botKey(value: String) = Key.key(namespace, value)

val Interaction.context: Context
    get() = Context.fromInteraction(this)

val Message.context: Context
    get() = Context.fromMessage(this)

val MessageChannel.context: Context
    get() = Context.fromChannel(this)

suspend fun Context.text(value: Key, vararg args: Any?): String =
    Text.single(value, *args).asString(this).awaitSingle()

suspend fun Context.text(value: String, vararg args: Any?): String =
    text(botKey(value), *args)

fun <E> MutableList<E>.removeFirst(n: Int): List<E> {
    require(n >= 0) { "Requested element count $n is less than zero." }
    if (n == 0) return emptyList()
    if (n >= size) {
        val newList = toList()
        clear()
        return newList
    }
    if (n == 1) return listOf(removeFirst())

    val list = ArrayList<E>(n)
    while (list.size < n) {
        list.add(removeFirst())
    }
    return list
}