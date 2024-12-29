package dev.qixils.quasicolon.bot.moderation

import dev.minn.jda.ktx.coroutines.await
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.removeFirst
import dev.qixils.quasicolon.bot.text
import dev.qixils.quasicord.decorators.option.Contextual
import dev.qixils.quasicord.decorators.option.Option
import dev.qixils.quasicord.decorators.slash.DefaultPermissions
import dev.qixils.quasicord.decorators.slash.SlashCommand
import dev.qixils.quasicord.decorators.slash.SlashSubCommand
import dev.qixils.quasicord.locale.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.concurrent.CompletableFuture

@SlashCommand("clear", guildOnly = true)
@DefaultPermissions(Permission.MESSAGE_MANAGE)
class ClearCommand(private val quasicolon: Quasicolon) {

    companion object {
        private const val PURGE_AMOUNT = 100
    }

    @SlashSubCommand("count")
    fun clearCount(
        @Option(value = "amount", type = OptionType.INTEGER) amount: Int,
        @Contextual interaction: SlashCommandInteractionEvent,
        @Contextual ctx: Context,
    ) { quasicolon.scope.launch {
        val channel = interaction.channel as? GuildMessageChannel ?: return@launch
        // TODO: bot perm check
        val hook = async { interaction.deferReply(true).await() }
        var queued = 0

        coroutineScope {
            val bulkQueue = mutableListOf<Message>()
            val history = interaction.channel.history
            while (queued < amount) {
                val toFetch = (amount - queued).coerceAtMost(PURGE_AMOUNT)
                queued += toFetch
                bulkQueue.addAll(history.retrievePast(toFetch).await())
                if (bulkQueue.size >= PURGE_AMOUNT) {
                    val toDelete = bulkQueue.removeFirst(PURGE_AMOUNT)
                    launch { CompletableFuture.allOf(*channel.purgeMessages(toDelete).toTypedArray()).await() }
                }
            }
            launch { CompletableFuture.allOf(*channel.purgeMessages(bulkQueue).toTypedArray()).await() }
        }

        hook.await().editOriginal(ctx.text("clear.output.ok", queued)).await()
    } }

    @SlashSubCommand("after")
    fun clearAfter(
        @Option(value = "when", type = OptionType.INTEGER) snowflake: Long,
        @Contextual interaction: SlashCommandInteractionEvent,
        @Contextual ctx: Context,
    ) { quasicolon.scope.launch {
        val channel = interaction.channel as? GuildMessageChannel ?: return@launch
        // TODO: bot perm check
        val hook = async { interaction.deferReply(true).await() }
        var queued = 0

        coroutineScope {
            val bulkQueue = mutableListOf<Message>()
            val history = interaction.channel.history
            while (true) {
                val fetched = history.retrievePast(PURGE_AMOUNT).await()
                val filtered = fetched.takeWhile { it.idLong >= snowflake }
                queued += filtered.size
                bulkQueue.addAll(filtered)
                if (filtered.size != fetched.size) break
                if (bulkQueue.size >= PURGE_AMOUNT) {
                    val toDelete = bulkQueue.removeFirst(PURGE_AMOUNT)
                    launch { CompletableFuture.allOf(*channel.purgeMessages(toDelete).toTypedArray()).await() }
                }
            }
            launch { CompletableFuture.allOf(*channel.purgeMessages(bulkQueue).toTypedArray()).await() }
        }

        hook.await().editOriginal(ctx.text("clear.output.ok", queued)).await()
    } }
}