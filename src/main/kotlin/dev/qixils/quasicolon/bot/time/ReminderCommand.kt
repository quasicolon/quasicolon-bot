package dev.qixils.quasicolon.bot.time

import dev.minn.jda.ktx.coroutines.await
import dev.qixils.quasicolon.bot.*
import dev.qixils.quasicord.decorators.option.Contextual
import dev.qixils.quasicord.decorators.option.Option
import dev.qixils.quasicord.decorators.slash.SlashCommand
import dev.qixils.quasicord.decorators.slash.SlashSubCommand
import dev.qixils.quasicord.locale.Context
import dev.qixils.quasicord.text.Text
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import reactor.core.publisher.Flux
import java.time.Instant

@SlashCommand("remind")
class ReminderCommand(private val quasicolon: Quasicolon) {

    companion object {
        private const val JOIN_ID = "reminder_join"
    }

    init {
        Flux.from(quasicolon.databaseManager.collection(Reminder::class.java).find())
            .subscribe { reminder -> ReminderTask(quasicolon, reminder).schedule() }
    }

    @SlashSubCommand("set")
    fun setReminder(
        @Option(value = "when", type = OptionType.STRING) instant: Instant,
        @Option(value = "note", required = false, type = OptionType.STRING) note: String?,
        @Contextual interaction: SlashCommandInteraction,
        @Contextual ctx: Context,
    ) = runBlocking {
        val text = ctx.text("remind.set.output.ok", Timestamp.RELATIVE.format(instant), Timestamp.SHORT_FULL.format(instant))
        val message = interaction.reply(text).await().retrieveOriginal().await()
        val reminder = Reminder(interaction.user.idLong, Instant.now(), instant, MessageLink.of(message), note)
        quasicolon.databaseManager.collection(Reminder::class.java).insertOne(reminder).awaitSingle()
        ReminderTask(quasicolon, reminder).schedule()
    }

    @SubscribeEvent
    fun onButton(event: ButtonInteractionEvent) {
        if (event.componentId != JOIN_ID) return

        // TODO
    }
}
