package dev.qixils.quasicolon.bot.time

import com.mongodb.client.model.Filters
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.asDisabled
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import dev.qixils.quasicolon.bot.MessageLink
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.context
import dev.qixils.quasicolon.bot.text
import dev.qixils.quasicord.decorators.option.Contextual
import dev.qixils.quasicord.decorators.option.Option
import dev.qixils.quasicord.decorators.slash.SlashCommand
import dev.qixils.quasicord.decorators.slash.SlashSubCommand
import dev.qixils.quasicord.locale.Context
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.time.Instant

@SlashCommand("remind")
class ReminderCommand(private val quasicolon: Quasicolon) {

    companion object {
        private const val JOIN_ID = "reminder_join"
    }

    init {
        quasicolon.databaseManager
            .getAll(Reminder::class.java)
            .subscribe { reminder -> ReminderTask(quasicolon, reminder).schedule() }
    }

    @SlashSubCommand("set")
    fun setReminder(
        @Option(value = "when", type = OptionType.STRING) instant: Instant,
        @Option(value = "note", required = false, type = OptionType.STRING) note: String?,
        @Contextual interaction: SlashCommandInteraction,
        @Contextual ctx: Context,
    ) = quasicolon.scope.launch {
        val text = ctx.text("remind.set.output.ok", Timestamp.RELATIVE.format(instant), Timestamp.SHORT_FULL.format(instant))
        val message = interaction.reply_(text, components = listOf(row(button(
            JOIN_ID,
            ctx.text("remind.set.output.buttons.join"),
            style = ButtonStyle.PRIMARY,
        )))).await().retrieveOriginal().await() // TODO: probably not necessary? what is reply.idLong?

        val reminder = Reminder(interaction.user.idLong, Instant.now(), instant, MessageLink.of(message), note)
        quasicolon.databaseManager.collection(Reminder::class.java).insertOne(reminder).awaitSingle()
        ReminderTask(quasicolon, reminder).schedule()
    }

    @SubscribeEvent
    fun onButton(event: ButtonInteractionEvent) = quasicolon.scope.launch {
        if (event.componentId != JOIN_ID) return@launch
        val ctx = event.context

        val reminder = quasicolon.databaseManager.getAllBy(Filters.eq("where.message", event.messageIdLong), Reminder::class.java).awaitFirstOrNull()

        if (reminder == null) {
            launch { event.editComponents(event.message.components.asDisabled()).await() }
            launch { event.reply_(ctx.text("remind.join.output.elapsed"), ephemeral = true).await() }
            return@launch
        }

        val text = ctx.text("remind.set.output.ok", Timestamp.RELATIVE.format(reminder.end), Timestamp.SHORT_FULL.format(reminder.end))
        launch { event.reply_(text, ephemeral = true).await() }
        val newReminder = Reminder(event.user.idLong, Instant.now(), reminder.end, reminder.where, reminder.note)
        quasicolon.databaseManager.collection(Reminder::class.java).insertOne(newReminder).awaitSingle()
        ReminderTask(quasicolon, newReminder).schedule()
    }
}
