package dev.qixils.quasicolon.bot.moderation

import com.mongodb.client.model.Filters
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.asDisabled
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.editMessage_
import dev.minn.jda.ktx.messages.reply_
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.context
import dev.qixils.quasicolon.bot.text
import dev.qixils.quasicord.decorators.ContextCommand
import dev.qixils.quasicord.locale.Context
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction
import net.dv8tion.jda.api.interactions.components.ActionRow

class ReportCommand(private val quasicolon: Quasicolon) {

    private val pendingReports = mutableMapOf<Long, MessageContextInteraction>()

    @ContextCommand(value = "report_msg", type = Command.Type.MESSAGE, guildOnly = true)
    fun onReport(
        interaction: MessageContextInteraction,
        ctx: Context,
    ) = runBlocking {
        if (ctx.guild() == 0L) return@runBlocking

        val msg = interaction.target

        val rules = quasicolon.databaseManager.collection(ServerRules::class.java).find(Filters.eq("guild", ctx.guild())).awaitSingle()

        val reply = interaction.reply_(
            ctx.text("report_msg.output.dm.body"),
            components = listOf(
                ActionRow.of(
                    StringSelectMenu(
                        "report_${msg.id}_rule",
                        options = rules.items.mapIndexed { index, item -> SelectOption(item.label, "rule_$index", item.description) }
                            + SelectOption(ctx.text("report_msg.output.rules.other"), "rule_other", default = true), // TODO: i18n
                    ),
                ),
                ActionRow.of(
                    button(
                        "report_${msg.id}_cancel",
                        ctx.text("report_msg.output.dm.buttons.cancel"),
                    ),
                ),
            ),
            ephemeral = true,
        ).await()

        pendingReports[reply.idLong] = interaction // TODO: praying this is the right ID?
        // TODO: timeout after 15mins? some jda docs mentioned discord might not keep hooks past this time
    }

    @SubscribeEvent
    fun onSelectRule(
        interaction: ButtonInteractionEvent,
    ) = runBlocking {
        if (!interaction.componentId.startsWith("rule_")) return@runBlocking
        val ctx = interaction.context

        interaction.editComponents(interaction.message.components.asDisabled()).queue()

        val origin = pendingReports.remove(interaction.messageIdLong)
        if (origin == null) {
            interaction.reply(ctx.text("report_msg.output.dm.timeout")).await()
            return@runBlocking
        }

        // TODO: send report
    }
}