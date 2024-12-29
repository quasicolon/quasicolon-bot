package dev.qixils.quasicolon.bot.moderation

import dev.qixils.quasicord.db.CollectionName
import org.bson.codecs.pojo.annotations.BsonId
import org.jetbrains.annotations.ApiStatus

@CollectionName("report_config")
data class ReportConfig @ApiStatus.Internal constructor(
    /**
     * Guild ID
     */
    @field:BsonId @param:BsonId val id: Long,
    val rules: MutableList<String> = mutableListOf(),
    var modmail: Long = 0,
)
