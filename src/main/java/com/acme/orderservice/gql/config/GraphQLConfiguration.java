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
 *  GraphQLConfiguration.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.acme.orderservice.gql.config;

import com.acme.orderservice.config.ServiceConfig;
import com.acme.orderservice.util.GraphqlSchemaReaderUtil;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.neo4j.graphql.DataFetchingInterceptor;
import org.neo4j.graphql.SchemaBuilder;
import org.neo4j.graphql.SchemaConfig;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ailegorreta.data.neo4j.gql.scalars.LocalDateTimeScalar;
import com.ailegorreta.data.neo4j.gql.scalars.LocalDateScalar;

import java.io.IOException;
import java.util.List;

/**
 * From the example: https://github.com/neo4j-graphql/neo4j-graphql-java/tree/master/examples/graphql-spring-boot
 * we register the Neo4j graphql schema.
 *
 * @project order-service
 * @author rlh
 * @date November 2023
 */
@Configuration
public class GraphQLConfiguration {

    @Bean
    public GraphQlSourceBuilderCustomizer graphQlSourceBuilderCustomizer(ServiceConfig serviceConfig,
                                                                         DataFetchingInterceptor dataFetchingInterceptor) throws IOException {
        String schema = GraphqlSchemaReaderUtil.getSchemaFromFileName(serviceConfig.getGraphql());
        TypeDefinitionRegistry neo4jTypeDefinitionRegistry = new SchemaParser().parse(schema);
        SchemaBuilder schemaBuilder = new SchemaBuilder(neo4jTypeDefinitionRegistry,
                new SchemaConfig(new SchemaConfig.CRUDConfig(),
                                 new SchemaConfig.CRUDConfig(true, List.of()),
                                false,
                                true,
                                SchemaConfig.InputStyle.INPUT_TYPE,
                        false, false));
        schemaBuilder.augmentTypes();

        return builder -> builder
                .configureRuntimeWiring(runtimeWiringBuilder -> {
                    runtimeWiringBuilder.scalar(LocalDateTimeScalar.graphQLScalarType());
                    runtimeWiringBuilder.scalar(LocalDateScalar.graphQLScalarType());
                    schemaBuilder.registerTypeNameResolver(runtimeWiringBuilder);
                    schemaBuilder.registerScalars(runtimeWiringBuilder);

                    GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

                    schemaBuilder.registerDataFetcher(codeRegistryBuilder, dataFetchingInterceptor);
                    runtimeWiringBuilder.codeRegistry(codeRegistryBuilder);
                })
                .schemaFactory((typeDefinitionRegistry, runtimeWiring) -> {
                    typeDefinitionRegistry.merge(neo4jTypeDefinitionRegistry);

                    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
                });
    }
}