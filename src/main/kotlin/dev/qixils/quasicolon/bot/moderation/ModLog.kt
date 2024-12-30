package dev.qixils.quasicolon.bot.moderation

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import dev.qixils.quasicolon.bot.*
import dev.qixils.quasicord.locale.Context
import dev.qixils.quasicord.utils.ContextualEmoji
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData

class ModLog(private val quasicolon: Quasicolon) {

    private suspend fun Context.userString(user: User) = text("log.user", user.asMention, user.name)
    private val joinEmoji = ContextualEmoji("⮕", 465369329836228618)
    private val leaveEmoji = ContextualEmoji("\uD83D\uDEEB", 465369329324523542)

    private fun log(guild: Guild, channelType: ChannelType, textSupplier: suspend (GuildMessageChannel) -> MessageCreateData) { Quasicolon.scope.launch {
        // TODO: queue
        // TODO: allowed mentions
        val destination = quasicolon.getChannel(guild, channelType) ?: return@launch
        if (!destination.botHas(Permission.MESSAGE_SEND)) return@launch

        val message = textSupplier.invoke(destination)

        try {
            destination.sendMessage(message).await()
        } catch (e: Exception) {
            quasicolon.logger.warn("Unable to send mod log", e)
        }
    } }

    @SubscribeEvent
    fun onJoin(event: GuildMemberJoinEvent) = log(event.guild, ChannelType.MEMBERSHIP) {
        MessageCreate(buildString {
            val ctx = it.context
            append(joinEmoji.getEmoji(it).formatted)
            append(' ')
            append(ctx.text("log.join", ctx.userString(event.user)))
        })
    }

    @SubscribeEvent
    fun onLeave(event: GuildMemberRemoveEvent) = log(event.guild, ChannelType.MEMBERSHIP) {
        MessageCreate(buildString {
            val ctx = it.context
            append(leaveEmoji.getEmoji(it).formatted)
            append(' ')
            append(ctx.text("log.quit", ctx.userString(event.user)))
        })
    }

    @SubscribeEvent
    fun onRoleAdd(event: GuildMemberRoleAddEvent) = log(event.guild, ChannelType.ROLES) {
        MessageCreate(buildString {
            val ctx = it.context
            val user = ctx.userString(event.user)
            for (role in event.roles) {
                append(ctx.text("log.role_join", user, role.asMention))
                appendLine()
            }
        }.trimEnd())
    }

    @SubscribeEvent
    fun onRoleRemove(event: GuildMemberRoleRemoveEvent) = log(event.guild, ChannelType.ROLES) {
        MessageCreate(buildString {
            val ctx = it.context
            val user = ctx.userString(event.user)
            for (role in event.roles) {
                append(ctx.text("log.role_quit", user, role.asMention))
                appendLine()
            }
        }.trimEnd())
    }

    @SubscribeEvent
    fun onChangeName(event: UserUpdateNameEvent) { Quasicolon.scope.launch {
        for (guild in quasicolon.jda.guilds) {
            guild.retrieveMember(event.user).await() ?: continue
            log(guild, ChannelType.NAMES) {
                val ctx = it.context
                MessageCreate(ctx.text("log.name", event.user.asMention, event.oldName, event.newName))
            }
        }
    } }

    @SubscribeEvent
    fun onEdit(event: MessageUpdateEvent) {
        if (!event.isFromGuild) return
        // TODO
    }

    @SubscribeEvent
    fun onDelete(event: MessageDeleteEvent) {
        if (!event.isFromGuild) return
        // TODO
    }
}