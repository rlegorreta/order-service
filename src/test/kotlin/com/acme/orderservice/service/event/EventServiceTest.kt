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
 *  EventServiceTest.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.acme.orderservice.service.event

import com.acme.orderservice.dto.OrderDTO
import com.ailegorreta.commons.utils.HasLogger
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Service


/**
 * This service test is just to emulate how we can send an from for 'Python ingestor' in order that the
 * order-service listen to it and inserts a new Order
 *
 * It does NOT have to do anything with testing the EvenService.class
 *
 * @project: order-service
 * @autho: rlh
 * @date November 2023
 */
@Service
class EventServiceTest(private val streamBridge: StreamBridge,
                       private val mapper: ObjectMapper): HasLogger {

    /**
     * This method sends an event using Spring cloud stream, i.e., streamBridge instance
     */
    fun sendEvent(value: OrderDTO): OrderDTO {
        logger.debug("Will send use stream bridge:$streamBridge")

        val res = streamBridge.send("producerTest-out-0", mapper.writeValueAsString(value))
        logger.debug("Result for sending the message via streamBridge:$res")

        return value
    }
}