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
 *  AdditionalDataFetcher.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.acme.orderservice.gql.datafetcher;

import com.acme.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
class AdditionalDataFetcher {
    private final OrderRepository orderRepository;

    AdditionalDataFetcher(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @SchemaMapping(typeName = "Movie", field = "bar")
    public String bar() {
        return "foo";
    }

    @SchemaMapping(typeName = "Movie", field = "javaData")
    public List<JavaData> javaData(DataFetchingEnvironment env) {
        // no inspection unchecked
        Object title = ((Map<String, Object>) env.getSource()).get("title");
        return Collections.singletonList(new JavaData("test " + title));
    }

    @QueryMapping
    public String ordersCount() {
        return orderRepository.count() + "";
    }

    public static class JavaData {
        @JsonProperty("name")
        public String name;

        public JavaData(String name) {
            this.name = name;
        }
    }
}