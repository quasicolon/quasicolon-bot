package dev.qixils.quasicolon.bot.moderation

import dev.qixils.quasicord.db.CollectionName
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.jetbrains.annotations.ApiStatus

@JvmRecord
data class ServerRule(
    val label: String,
    val description: String?,
)

@CollectionName("rules")
@JvmRecord
data class ServerRules @ApiStatus.Internal constructor(
    @field:BsonId @param:BsonId val id: ObjectId,
    val guild: Long,
    val items: MutableList<ServerRule>, // rational for storing like this is to allow sorting
) {
    constructor(guild: Long, items: MutableList<ServerRule>)
            : this(ObjectId.get(), guild, items)
}
