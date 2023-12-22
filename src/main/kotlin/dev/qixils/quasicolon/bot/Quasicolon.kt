package dev.qixils.quasicolon.bot

import dev.qixils.quasicolon.bot.time.ReminderCommand
import dev.qixils.quasicord.Quasicord
import net.dv8tion.jda.api.entities.Activity
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.io.path.Path

const val namespace: String = "quasicolon"

fun main(args: Array<String>) {
    Quasicolon.jda.awaitShutdown()
}

object Quasicolon : Quasicord(
    namespace,
    listOf(Locale.ENGLISH),

    // todo: why is this all coded like this
    Path("."),
    Activity.listening("/help"),
    null
) {
    val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(6)

    override fun registerCommands() {
        super.registerCommands()
        commandManager.discoverCommands(ReminderCommand(this))
    }
}