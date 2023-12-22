package dev.qixils.quasicolon.bot.time

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.send
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.botKey
import dev.qixils.quasicord.locale.Context
import dev.qixils.quasicord.text.Text
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class ReminderTask(val quasicolon: Quasicolon, val reminder: Reminder) : () -> Unit {
    private val logger = LoggerFactory.getLogger(ReminderTask::class.java)

    override fun invoke() = runBlocking {
        val channel = quasicolon.jda.openPrivateChannelById(reminder.user).await() ?: run {
            logger.warn("Unable to find user {} to send reminder", reminder.user)
            return@runBlocking
        }

        var key = "remind.set.output.dm"
        val args: MutableList<Any?> = mutableListOf(Timestamp.RELATIVE.format(reminder.start), reminder.where)
        if (reminder.note == null)
            key += "-no-note"
        else
            args += reminder.note

        val context = Context.builder()
            .user(reminder.user)
            .channel(reminder.where.channel)
            .guild(reminder.where.guild)
            .build()
        val text = Text.single(botKey(key), *args.toTypedArray()).asString(context).awaitSingle()

        try {
            channel.sendMessage(text).await()
            quasicolon.databaseManager.collection(Reminder::class.java)
                .deleteOne(eq("_id", reminder.id))
                .awaitSingle()
        } catch (e: ErrorResponseException) {
            logger.warn("Unable to message user {} to send reminder", reminder.user)
        }
    }

    fun schedule() {
        quasicolon.schedule(Duration.between(Instant.now(), reminder.end).seconds, TimeUnit.SECONDS, this)
    }
}