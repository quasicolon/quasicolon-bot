package dev.qixils.quasicolon.bot.time

import com.mongodb.client.model.Filters.eq
import dev.minn.jda.ktx.coroutines.await
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.botKey
import dev.qixils.quasicord.locale.Context
import dev.qixils.quasicord.text.Text
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class ReminderTask(val quasicolon: Quasicolon, val originalReminder: Reminder) : () -> Unit {
    private val logger = LoggerFactory.getLogger(ReminderTask::class.java)

    override fun invoke() { quasicolon.scope.launch {
        val reminder = quasicolon.databaseManager.getById(originalReminder.id, Reminder::class.java).awaitSingle()

        val channel = quasicolon.jda.openPrivateChannelById(reminder.user).await() ?: run {
            logger.warn("Unable to find user {} to send reminder", reminder.user)
            return@launch
        }


        if (reminder.cancelled != true) {
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
            } catch (e: ErrorResponseException) {
                logger.warn("Unable to message user {} to send reminder", reminder.user)
                return@launch
            }
        }

        quasicolon.databaseManager.collection(Reminder::class.java)
            .deleteOne(eq("_id", reminder.id))
            .awaitSingle()
    } }

    fun schedule() {
        quasicolon.schedule(Duration.between(Instant.now(), originalReminder.end).seconds, TimeUnit.SECONDS, this)
    }
}