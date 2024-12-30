package dev.qixils.quasicolon.bot

import dev.minn.jda.ktx.generics.getChannel
import dev.qixils.quasicolon.bot.moderation.ClearCommand
import dev.qixils.quasicolon.bot.moderation.ModLog
import dev.qixils.quasicolon.bot.moderation.ReportCommand
import dev.qixils.quasicolon.bot.moderation.RulesCommand
import dev.qixils.quasicolon.bot.time.ReminderCommand
import dev.qixils.quasicord.Quasicord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import java.time.Duration
import java.util.*
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
    companion object {
        private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(8)
        val scope = CoroutineScope(Dispatchers.Default)
    }

    private fun discover(error: String, runnable: () -> Unit) {
        try {
            runnable.invoke()
        } catch (e: Exception) {
            logger.error(error, e)
        }
    }
    private fun discoverEvent(supplier: () -> Any) = discover("Failed to register events") { jda.addEventListener(supplier) }
    private fun discoverCommand(supplier: () -> Any) = discover("Failed to register command") { commandManager.discoverCommands(supplier) }

    init {
        discoverEvent { ModLog(this) }
    }

    override fun registerCommands() {
        super.registerCommands()
        discoverCommand { ReminderCommand(this) }
        discoverCommand { ReportCommand(this) }
        discoverCommand { ClearCommand(this) }
        discoverCommand { RulesCommand(this) }
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

    suspend fun getChannel(guild: Guild, channelType: ChannelType): GuildMessageChannel? {
        val filter = mapOf("guild" to guild.idLong, "type" to channelType)
        val entry = databaseManager.getAllByEquals(filter, ChannelConfig::class.java).awaitFirstOrNull() ?: return null
        return guild.getChannel<GuildMessageChannel>(entry.item)
    }
}