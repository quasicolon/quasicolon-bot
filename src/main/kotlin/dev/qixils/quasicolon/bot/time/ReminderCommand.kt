package dev.qixils.quasicolon.bot.time

import dev.minn.jda.ktx.coroutines.await
import dev.qixils.quasicolon.bot.MessageLink
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.botKey
import dev.qixils.quasicord.decorators.option.Contextual
import dev.qixils.quasicord.decorators.option.Option
import dev.qixils.quasicord.decorators.slash.SlashCommand
import dev.qixils.quasicord.decorators.slash.SlashSubCommand
import dev.qixils.quasicord.locale.Context
import dev.qixils.quasicord.text.Text
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import reactor.core.publisher.Flux
import java.time.Instant

@SlashCommand("remind")
class ReminderCommand(val quasicolon: Quasicolon) {

    init {
        Flux.from(quasicolon.databaseManager.collection(Reminder::class.java).find())
            .subscribe { reminder -> ReminderTask(reminder).schedule() }
    }

    @SlashSubCommand("set")
    fun setReminder(
        @Option(value = "when", type = OptionType.STRING) `when`: Instant,
        @Option(value = "note", required = false, type = OptionType.STRING) note: String?,
        @Contextual interaction: SlashCommandInteraction
    ) = runBlocking {
        val text = Text.single(botKey("remind.set.output.ok"), Timestamp.RELATIVE.format(`when`), Timestamp.SHORT_FULL.format(`when`))
            .asString(Context.fromInteraction(interaction))
            .awaitSingle()
        val message = interaction.reply(text).await().retrieveOriginal().await()
        // TODO: something is failing beyond this point
        val reminder = Reminder(message.author.idLong, Instant.now(), `when`, MessageLink.of(message), note)
        Quasicolon.databaseManager.collection(Reminder::class.java).insertOne(reminder)
        ReminderTask(reminder).schedule()
    }
}
