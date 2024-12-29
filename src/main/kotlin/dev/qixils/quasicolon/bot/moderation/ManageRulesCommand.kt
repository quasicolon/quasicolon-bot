package dev.qixils.quasicolon.bot.moderation

import com.mongodb.client.model.Filters
import dev.minn.jda.ktx.coroutines.await
import dev.qixils.quasicolon.bot.Quasicolon
import dev.qixils.quasicolon.bot.text
import dev.qixils.quasicord.decorators.option.Contextual
import dev.qixils.quasicord.decorators.option.Option
import dev.qixils.quasicord.decorators.slash.DefaultPermissions
import dev.qixils.quasicord.decorators.slash.SlashCommand
import dev.qixils.quasicord.decorators.slash.SlashSubCommand
import dev.qixils.quasicord.locale.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

@SlashCommand("manage_rules", guildOnly = true)
@DefaultPermissions(Permission.MESSAGE_MANAGE)
class ManageRulesCommand(private val quasicolon: Quasicolon) {

    // TODO: maybe forms would be best here? could enforce character limits i think

    @SlashSubCommand("add")
    fun addRule(
        @Option(value = "label", type = OptionType.STRING) label: String,
        @Option(value = "description", type = OptionType.STRING, required = false) description: String?,
        @Contextual interaction: SlashCommandInteractionEvent,
        @Contextual ctx: Context,
    ) { Quasicolon.scope.launch {
        val hook = async { interaction.deferReply(true).await() }

        val filter = Filters.eq("guild", interaction.guild!!.idLong)
        val collection = quasicolon.databaseManager.collection(ServerRules::class.java)
        val rules = collection.find(filter).awaitFirstOrNull()
            ?: ServerRules(interaction.guild!!.idLong)

        rules.items.add(ServerRule(label, description))
        collection.replaceOne(filter, rules).awaitFirstOrNull()

        hook.await().editOriginal(ctx.text("manage_rules.add.output.ok")).await()
    } }

    @SlashSubCommand("set")
    fun setRule(
        @Option(value = "index", type = OptionType.INTEGER) index: Int, // TODO: some sort of dropdown/autocompleter should be possible here
        @Option(value = "label", type = OptionType.STRING) label: String,
        @Option(value = "description", type = OptionType.STRING, required = false) description: String?,
        @Contextual interaction: SlashCommandInteractionEvent,
        @Contextual ctx: Context,
    ) { Quasicolon.scope.launch {
        val hook = async { interaction.deferReply(true).await() }

        val filter = Filters.eq("guild", interaction.guild!!.idLong)
        val collection = quasicolon.databaseManager.collection(ServerRules::class.java)
        val rules = collection.find(filter).awaitFirstOrNull()
        if (rules == null || index >= rules.items.size) {
            hook.await().editOriginal(ctx.text("manage_rules.set.output.none")).await()
            return@launch
        }

        rules.items[index] = ServerRule(label, description)
        collection.replaceOne(filter, rules).awaitFirstOrNull()

        hook.await().editOriginal(ctx.text("manage_rules.set.output.ok")).await()
    } }
}