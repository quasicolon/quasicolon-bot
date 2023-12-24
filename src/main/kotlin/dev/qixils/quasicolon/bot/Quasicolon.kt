package dev.qixils.quasicolon.bot

import dev.qixils.quasicolon.bot.time.ReminderCommand
import dev.qixils.quasicord.Quasicord
import net.dv8tion.jda.api.entities.Activity
import java.time.Duration
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

const val namespace: String = "quasicolon"

fun main(args: Array<String>) {
    Quasicolon().jda.awaitShutdown()
}

class Quasicolon : Quasicord(
    namespace,
    Locale.ENGLISH,

    // todo: why is this all coded like this
    Path("."),
    Activity.listening("/help"),
    null
) {
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(6)

    override fun registerCommands() {
        super.registerCommands()
        commandManager.discoverCommands(ReminderCommand(this))
    }

    fun schedule(duration: Long, unit: TimeUnit, runnable: () -> Unit) {
        executor.schedule({
            try {
                runnable()
            } catch (e: Exception) {
                logger.warn("Failed to execute scheduled task", e)
            }
        }, duration, unit)
    }

    fun schedule(duration: Duration, runnable: () -> Unit) {
        schedule(duration.toMillis(), TimeUnit.MILLISECONDS, runnable)
    }

    fun schedule(duration: kotlin.time.Duration, runnable: () -> Unit) {
        schedule(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS, runnable)
    }
}