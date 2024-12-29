package dev.qixils.quasicolon.bot.time

import dev.qixils.quasicolon.bot.MessageLink
import dev.qixils.quasicord.db.CollectionName
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.jetbrains.annotations.ApiStatus
import java.time.Instant

@CollectionName("reminder")
@JvmRecord
data class Reminder @ApiStatus.Internal constructor(
    @field:BsonId @param:BsonId val id: ObjectId,
    val user: Long,
    val start: Instant,
    val end: Instant,
    val where: MessageLink,
    val note: String?,
    val cancelled: Boolean?,
) {
    constructor(user: Long, start: Instant, end: Instant, where: MessageLink, note: String? = null, cancelled: Boolean? = null)
            : this(ObjectId.get(), user, start, end, where, note, cancelled)
}
