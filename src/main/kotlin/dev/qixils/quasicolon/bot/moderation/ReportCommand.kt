package dev.qixils.quasicolon.bot.moderation

import com.mongodb.client.model.Filters
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.editMessage_
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import dev.qixils.quasicolon.bot.*
import dev.qixils.quasicord.decorators.ContextCommand
import dev.qixils.quasicord.locale.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitSingle
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction
import net.dv8tion.jda.api.interactions.components.ActionRow

class ReportCommand(private val quasicolon: Quasicolon) {

    private val pendingReports = mutableMapOf<Long, MessageContextInteraction>()
    private val rulePattern = Regex("^rule_(.+)$")

    // TODO: replace awaitSingle with awaitFirstOrNull + error handlers

    @ContextCommand(value = "report_msg", type = Command.Type.MESSAGE, guildOnly = true)
    fun onReport(
        interaction: MessageContextInteraction,
        ctx: Context,
    ) = quasicolon.scope.launch {
        if (ctx.guild() == 0L) return@launch

        val msg = interaction.target

        val rules = quasicolon.databaseManager.getAllBy(Filters.eq("guild", ctx.guild()), ServerRules::class.java).awaitSingle()

        val reply = interaction.reply_(
            ctx.text("report_msg.output.dm.body"),
            components = listOf(
                ActionRow.of(
                    StringSelectMenu(
                        "report_rule",
                        options = rules.items.mapIndexed { index, item -> SelectOption(item.label, "rule_$index", item.description) }
                            + SelectOption(ctx.text("report_msg.output.rules.other"), "rule_other", default = true), // TODO: i18n
                        placeholder = ctx.text("report_msg.output.dm.buttons.placeholder"),
                    ),
                ),
                ActionRow.of(
                    button(
                        id = "report_cancel",
                        label = ctx.text("report_msg.output.dm.buttons.cancel"),
                    ),
                    link(
                        url = msg.jumpUrl,
                        label = ctx.text("report_msg.output.dm.buttons.jump"),
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
        interaction: StringSelectInteractionEvent,
    ) = quasicolon.scope.launch {
        // TODO: support cancel
        if (interaction.componentId != "report_rule") return@launch

        val reply = async { interaction.deferReply(false).await() }
        launch { interaction.editComponents(interaction.message.components.asDisabled()).await() } // TODO: skip jump? (lol)

        val ctx = interaction.context

        val match = rulePattern.matchEntire(interaction.values.first()) ?: return@launch // todo: error
        val ruleId = match.groups[1]?.value?.toIntOrNull() ?: -1

        val origin = pendingReports.remove(interaction.messageIdLong)
        if (origin == null) {
            interaction.reply(ctx.text("report_msg.output.dm.timeout")).await()
            return@launch
        }
        val target = origin.target
        val author = target.author
        val reporter = interaction.user

        val channel = async { interaction.guild!!.getChannel<GuildMessageChannel>(quasicolon.databaseManager.getAllByEquals(mapOf("guild" to ctx.guild(), "type" to ChannelType.MODMAIL), ChannelConfig::class.java).awaitSingle().item)!! } // todo error
        val ctxChannelAsync = async { channel.await().context }

        val rule = (if (ruleId != -1) {
            val rules = quasicolon.databaseManager.getAllByEquals(mapOf("guild" to ctx.guild()), ServerRules::class.java).awaitSingle()
            if (ruleId < rules.items.size) rules.items[ruleId].label
            else null
        } else null) ?: ctxChannelAsync.await().text("report_msg.output.rules.other")

        val ctxChannel = ctxChannelAsync.await()

        channel.await().send(embeds = listOf(
            Embed(
                title = ctxChannel.text("report_msg.output.embed.title"),
                url = target.jumpUrl,
                color = 0xde3d28,
                timestamp = target.timeCreated,
                description = target.contentDisplay,
            ) {
                field(
                    name = ctxChannel.text("report_msg.output.embed.poster"),
                    value = ctxChannel.text("report_msg.output.embed.user", author.asMention, author.name, author.id),
                    inline = false,
                )
                field(
                    name = ctxChannel.text("report_msg.output.embed.reporter"),
                    value = ctxChannel.text("report_msg.output.embed.user", reporter.asMention, reporter.name, reporter.id),
                    inline = false,
                )
                field(
                    name = ctxChannel.text("report_msg.output.embed.rule"),
                    value = rule,
                    inline = false,
                )
            }
        ))

        reply.await().editOriginal(ctx.text("report_msg.output.dm.submitted"))
    }
}