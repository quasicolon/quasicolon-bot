package dev.qixils.quasicolon.bot.moderation

import dev.minn.jda.ktx.coroutines.await
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.text
import dev.qixils.quasicord.decorators.option.Contextual
import dev.qixils.quasicord.decorators.slash.SlashCommand
import dev.qixils.quasicord.locale.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class RulesCommand(private val quasicolon: Quasicolon) {

    @SlashCommand("rules", guildOnly = true)
    fun listRules(
        @Contextual interaction: SlashCommandInteractionEvent,
        @Contextual ctx: Context,
    ) { Quasicolon.scope.launch {
        val hook = async { interaction.deferReply(true).await() }
        val rules = quasicolon.databaseManager.getAllByEquals(mapOf("guild" to interaction.guild!!.idLong), ServerRules::class.java).awaitFirstOrNull()
        if (rules == null || rules.items.isEmpty()) {
            hook.await().editOriginal(ctx.text("rules.output.none")).await()
            return@launch
        }
        val reply = buildString {
            rules.items.forEachIndexed { index, rule ->
                append(index + 1)
                append(". **")
                append(rule.label)
                append("**")
                if (!rule.description.isNullOrBlank()) {
                    append(' ')
                    append(rule.description)
                }
                appendLine()
            }
        }.trimEnd()
        hook.await().editOriginal(reply).await()
    } }
}