/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  EventServiceConsumer.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.acme.orderservice.service

import com.acme.orderservice.dto.OrderDTO
import com.ailegorreta.commons.utils.HasLogger
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.springframework.stereotype.Service
import java.util.concurrent.CountDownLatch

/**
 * Service event consumer
 *
 * @author rlh
 * @project : order-service
 * @date November 2023
 *
 */
@Service
class EventServiceConsumer(private val orderService: OrderService,
                           private val mapper: ObjectMapper): HasLogger {
    var latch = CountDownLatch(1)

    /**
     * This function receives an event from Kafka 'Python ingestor'' that we need to add a new Order
     */
    fun processEvent(event: String): String {

        try {
            val order = mapper.readValue(event, OrderDTO::class.java)

            orderService.addOrder(order)
            latch.countDown()       // just for testing purpose
        } catch (e: JsonProcessingException) {
            throw SerializationException(e)
        }

        return event
    }

}