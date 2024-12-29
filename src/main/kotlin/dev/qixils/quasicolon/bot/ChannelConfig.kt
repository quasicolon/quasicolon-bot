package dev.qixils.quasicolon.bot

import dev.qixils.quasicord.db.CollectionName
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.jetbrains.annotations.ApiStatus
import java.time.Instant

@CollectionName("channels")
@JvmRecord
data class ChannelConfig @ApiStatus.Internal constructor(
    @field:BsonId @param:BsonId val id: ObjectId,
    val guild: Long,
    val type: ChannelType,
    val item: Long,
) {
    constructor(guild: Long, type: ChannelType, item: Long)
            : this(ObjectId.get(), guild, type, item)
}
