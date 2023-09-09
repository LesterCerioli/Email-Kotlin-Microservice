package com.lts.web.rest

import java.util.HashMap
import java.util.Map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.http.ResponseEntity
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.GenericMessage
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import com.lts.config.KafkaSseConsumer
import com.lts.config.KafkaSseProducer

@RestController
@RequestMapping("/api/msemail-kafka")
class MsemailKafkaResource(@Qualifier(KafkaSseProducer.CHANNELNAME) val output: MessageChannel)  {

    private val log = LoggerFactory.getLogger(javaClass)

    private val sink: Sinks.Many<Message<String>> = Sinks.many().unicast().onBackpressureBuffer()

    @PostMapping("/publish")
    fun publish(@RequestParam message: String): Mono<ResponseEntity<Void>> {
        log.debug("REST request the message : {} to send to Kafka topic", message)
        val map = hashMapOf<String, Any>()
        map[MessageHeaders.CONTENT_TYPE] = MimeTypeUtils.TEXT_PLAIN_VALUE
        val headers = MessageHeaders(map)
        output.send(GenericMessage(message, headers))
        return Mono.just(ResponseEntity.noContent().build())
    }

    @GetMapping("/consume")
    fun consume(): Flux<String> {
        log.debug("REST request to consume records from Kafka topics")
        return sink.asFlux().map { m -> m.payload }
    }

    @StreamListener(value = KafkaSseConsumer.CHANNELNAME, copyHeaders = "false")
    fun consume(message: Message<String>) {
        log.debug("Got message from kafka stream: {}", message.getPayload())
        sink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST)
    }
}
