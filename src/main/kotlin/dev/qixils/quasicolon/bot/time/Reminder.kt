package dev.qixils.quasicolon.bot.time

import dev.qixils.quasicolon.bot.MessageLink
import dev.qixils.quasicord.db.CollectionName
import lombok.Getter
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
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
    val note: String?
) {
    constructor(user: Long, start: Instant, end: Instant, where: MessageLink, note: String?)
            : this(ObjectId.get(), user, start, end, where, note)
}
