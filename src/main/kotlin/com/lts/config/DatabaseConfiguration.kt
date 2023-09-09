
package com.lts.config

import tech.jhipster.config.JHipsterConstants
import io.r2dbc.spi.ConnectionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.CustomConversions
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.dialect.R2dbcDialect
import org.springframework.data.r2dbc.query.UpdateMapper
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.relational.core.dialect.RenderContextFactory
import org.springframework.data.relational.core.sql.render.SqlRenderer
import org.springframework.transaction.annotation.EnableTransactionManagement

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.ArrayList
import java.util.BitSet
import java.util.Collections
import java.util.List
import java.util.UUID

@Configuration
@EnableR2dbcRepositories("com.lts.repository")
@EnableTransactionManagement
class DatabaseConfiguration {



    /**
     * Simple singleton to convert [UUID]s to their [String] representation.
     */
    @WritingConverter
    object UUIDToStringConverter : Converter<UUID, String> {
        override fun convert(source: UUID) = source.toString()
    }

    /**
     * Simple singleton to convert from [String] [UUID] representation.
     */
    @ReadingConverter
    object StringToUUIDConverter : Converter<String, UUID> {
        override fun convert(source: String) =
            if (source == null) null else UUID.fromString(source)
    }

    // LocalDateTime seems to be the only type that is supported across all drivers atm
    // See https://github.com/r2dbc/r2dbc-h2/pull/139 https://github.com/mirromutth/r2dbc-mysql/issues/105
    @Bean
    fun r2dbcCustomConversions(dialect: R2dbcDialect): R2dbcCustomConversions {
        val converters = mutableListOf<Any>()
        converters.add(InstantWriteConverter)
        converters.add(InstantReadConverter)
        converters.add(BitSetReadConverter)
        converters.add(DurationWriteConverter)
        converters.add(DurationReadConverter)
        converters.add(ZonedDateTimeReadConverter)
        converters.add(ZonedDateTimeWriteConverter)
        converters.add(StringToUUIDConverter)
        converters.add(UUIDToStringConverter)
        return R2dbcCustomConversions.of(dialect, converters)
    }



    @Bean
    fun dialect(connectionFactory: ConnectionFactory) = DialectResolver.getDialect(connectionFactory)

    @Bean
    fun updateMapper(dialect: R2dbcDialect, mappingR2dbcConverter: MappingR2dbcConverter) = UpdateMapper(dialect, mappingR2dbcConverter)

    @Bean
    fun sqlRenderer(dialect: R2dbcDialect): SqlRenderer {
        val factory = RenderContextFactory(dialect)
        return SqlRenderer.create(factory.createRenderContext())
    }

    @WritingConverter
    object InstantWriteConverter: Converter<Instant, LocalDateTime> {
        override fun convert(source: Instant) = LocalDateTime.ofInstant(source, ZoneOffset.UTC)
    }

    @ReadingConverter
    object InstantReadConverter: Converter<LocalDateTime, Instant> {
        override fun convert(localDateTime: LocalDateTime) = localDateTime.toInstant(ZoneOffset.UTC)
    }

    @ReadingConverter
    object BitSetReadConverter : Converter<BitSet, Boolean> {
       override fun convert(bitSet: BitSet): Boolean = bitSet.get(0)
    }

    @ReadingConverter
    object ZonedDateTimeReadConverter : Converter<LocalDateTime, ZonedDateTime> {
       override fun convert(localDateTime: LocalDateTime) = ZonedDateTime.of(localDateTime, ZoneOffset.UTC)
    }

    @WritingConverter
    object ZonedDateTimeWriteConverter : Converter<ZonedDateTime, LocalDateTime> {
       override fun convert(zonedDateTime: ZonedDateTime) = zonedDateTime.toLocalDateTime()
    }

    @WritingConverter
    object DurationWriteConverter : Converter<Duration, Long> {
       override fun convert(source: Duration?) = if (source != null) source.toMillis() else null
    }

    @ReadingConverter
    object DurationReadConverter : Converter<Long, Duration> {
       override fun convert(source: Long?) = if(source != null) Duration.ofMillis(source) else null
    }

}
