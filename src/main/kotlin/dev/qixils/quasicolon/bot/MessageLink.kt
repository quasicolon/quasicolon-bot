package dev.qixils.quasicolon.bot

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReference

@JvmRecord
data class MessageLink(val guild: Long, val channel: Long, val message: Long) {

    override fun toString(): String {
        return "https://discord.com/channels/%d/%d/%d".format(guild, channel, message)
    }

    companion object {
        fun of(message: Message): MessageLink {
            return MessageLink(message.guildIdLong, message.channelIdLong, message.idLong)
        }

        fun of(message: MessageReference): MessageLink {
            return MessageLink(message.guildIdLong, message.channelIdLong, message.messageIdLong)
        }
    }
}
