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
 *  GraphQlTests.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.acme.orderservice.gcl;

import com.acme.orderservice.EnableTestContainers;
import com.acme.orderservice.model.Order;
import com.acme.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 *  For GraphQL tester see:
 *  https://piotrminkowski.com/2023/01/18/an-advanced-graphql-with-spring-boot/
 *
 *  - How graphQlTester is created (imperative):
 *  WebTestClient client = MockMvcWebTestClient.bindToApplicationContext(context)
 *                 .configureClient()
 *                 .baseUrl("/graphql")
 *                 .build();
 *
 * WebGraphQlTester tester = WebGraphQlTester.builder(client).build();
 *
 * - For WebFlux:
 * WebTestClient client = WebTestClient.bindToApplicationContext(context)
 *                 .configureClient()
 *                 .baseUrl("/graphql")
 *                 .build();
 *
 *  WebGraphQlTester tester = WebGraphQlTester.builder(client).build();
 *
 * - And last against a running remote server:
 * WebTestClient client =WebTestClient.bindToServer()
 *                 .baseUrl("http://localhost:8080/graphql")
 *                 .build();
 *
 * WebGraphQlTester tester = WebGraphQlTester.builder(client).build();
 *
 * note: Use always GraphQLTester and not WebMvc because GraphQL does not support WebMvc tester.
 *
 * @project order-service
 * @autho: rlh
 * @date: November 2023
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableTestContainers
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration-tests")
@DirtiesContext                /* will make sure this context is cleaned and reset between different tests */
@AutoConfigureGraphQlTester
public class GraphQLTests {

    @MockBean
    private StreamBridge streamBridge;
    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;			// Mocked the security JWT

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private ObjectMapper mapper;

    /*
    private final GraphQlTester graphQlTester;

    @Autowired
    public GraphQLTests(ExecutionGraphQlService graphQlService) {
        this.graphQlTester = ExecutionGraphQlServiceTester.builder(graphQlService).build();
    }

     */

    /**
     * Validates database initialization.
     */
    @Test
    void findAllOrders(@Autowired OrderRepository orderRepository) throws JsonProcessingException {
        /* Companies count */
        /*
        String queryCompanyCount = """
                    query {
                        companiasCount(nombre:"ACME SA de CV")
                    }
                """;
        var companiesCount = graphQlTester.document(queryCompanyCount)
                .execute()
                .path("data.companiasCount[*]")
                .matchesJson("""
                        [
                        ]
                        """);

        //assertThat(companiesCount.size()).isEqualTo(3);
         */
        var newOrder = new Order("", LocalDate.now().toString(),
                                "Polanco", "Computadora","2", "4000.0");

        orderRepository.save(newOrder);
        System.out.println(">>>>>> Ordenes:" + orderRepository.count());
        /* Query all orders */
        String query = """
                query {
                  orders  {
                    cantidad
                    fechaOperacion
                    monto
                    productoID
                    tiendaID               
                  }
                }
                """;
        graphQlTester.document(query)
                     .execute()
                     .path("orders")
                  //   .hasValue()
             //   .entityList(Estado.class)
             //   .get();
                    .matchesJson(mapper.writeValueAsString(newOrder));

        // assertThat(companies.size()).isEqualTo(7);
    }

}