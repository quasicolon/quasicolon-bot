package dev.qixils.quasicolon.bot.moderation

import dev.qixils.quasicolon.bot.MessageLink
import dev.qixils.quasicord.db.CollectionName
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.jetbrains.annotations.ApiStatus
import java.time.Instant

@CollectionName("report_config")
data class ReportConfig @ApiStatus.Internal constructor(
    /**
     * Guild ID
     */
    @field:BsonId @param:BsonId val id: Long,
    val rules: MutableList<String> = mutableListOf(),
    var modmail: Long = 0,
)
