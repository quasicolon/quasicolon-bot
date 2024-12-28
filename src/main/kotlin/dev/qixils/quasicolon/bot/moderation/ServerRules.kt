package dev.qixils.quasicolon.bot.moderation

import dev.qixils.quasicord.db.CollectionName
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.jetbrains.annotations.ApiStatus

@CollectionName("rules")
@JvmRecord
data class ServerRules @ApiStatus.Internal constructor(
    @field:BsonId @param:BsonId val id: ObjectId,
    val guild: Long,
    val items: MutableList<String>,
) {
    constructor(guild: Long, items: MutableList<String>)
            : this(ObjectId.get(), guild, items)
}
