package dev.qixils.quasicolon.bot.time

import java.time.Instant

enum class Timestamp(private val fmt: String) {

    /**
     * `9:01 AM`
     *
     * `09:01`
     */
    SHORT_TIME("<t:%d:t>"),

    /**
     * `9:01:00 AM`
     *
     *  `09:01:00`
     */
    LONG_TIME("<t:%d:T>"),

    /**
     * `11/28/2018`
     *
     * `28/11/2018`
     */
    SHORT_DATE("<t:%d:d>"),

    /**
     * `November 28, 2018`
     *
     * `28 November 2018`
     */
    LONG_DATE("<t:%d:D>"),

    /**
     * `November 28, 2018 9:01 AM`
     *
     * `28 November 2018 09:01`
     */
    SHORT_FULL("<t:%d:f>"),

    /**
     * `Wednesday, November 28, 2018 9:01 AM`
     *
     * `Wednesday, 28 November 2018 09:01`
     */
    LONG_FULL("<t:%d:F>"),

    /**
     * `3 years ago`
     */
    RELATIVE("<t:%d:R>");

    fun format(time: Long): String = fmt.format(time)
    fun format(time: Instant): String = format(time.epochSecond)
}