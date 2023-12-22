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
class Reminder @BsonCreator @ApiStatus.Internal constructor(
    @field:BsonId @param:BsonId val id: ObjectId,
    @param:BsonProperty("user") val user: Long,
    @param:BsonProperty("start") val start: Instant,
    @param:BsonProperty("end") val end: Instant,
    @param:BsonProperty("where") val where: MessageLink,
    @param:BsonProperty("note") val note: String?
) {
    constructor(user: Long, start: Instant, end: Instant, where: MessageLink, note: String?)
            : this(ObjectId.get(), user, start, end, where, note)
}
