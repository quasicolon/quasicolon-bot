package dev.qixils.quasicolon.bot.moderation

import com.mongodb.client.model.Filters
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.reply_
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.text
import dev.qixils.quasicord.decorators.ContextCommand
import dev.qixils.quasicord.locale.Context
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction
import net.dv8tion.jda.api.interactions.components.ActionRow

class ReportCommand(private val quasicolon: Quasicolon) {

    @ContextCommand(value = "report_msg", type = Command.Type.MESSAGE, guildOnly = true)
    fun onReport(
        interaction: MessageContextInteraction,
        ctx: Context,
    ) = runBlocking {
        if (ctx.guild() == 0L) return@runBlocking

        val msg = interaction.target

        val rules = quasicolon.databaseManager.collection(ServerRules::class.java).find(Filters.eq("guild", ctx.guild())).awaitSingle()

        val ask = interaction.reply_(
            ctx.text("report_msg.output.dm.body"),
            components = listOf(
                ActionRow.of(
                    StringSelectMenu(
                        "report_${msg.id}_rule",
                        options = rules.items.mapIndexed { index, item -> SelectOption(item, "rule_$index") },
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

        /*
        val embed = Embed {
            description = "[%s](%s)".format(
                ctx.text("report_msg.output.description"),
                interaction.target.jumpUrl
            )
            field {
                name =
            }
        }
        */
    }
}