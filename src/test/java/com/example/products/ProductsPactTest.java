package com.example.products;

import org.junit.jupiter.api.extension.ExtendWith;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "pactflow-example-provider-java-graphql")
public class ProductsPactTest {

  @Pact(consumer="pactflow-example-consumer-java-graphql")
  public RequestResponsePact getProduct(PactDslWithProvider builder) {

    PactDslJsonBody body = new PactDslJsonBody();
    body
      .object("data")
        .object("product")
          .stringType("id", "10")
          .stringType("name", "product name")
          .stringType("type", "product series")
        .closeObject()
      .closeObject();

    String query = """
product(id: "10") {
  id
  name
  type
}                
    """;          
      return builder
        .given("a product with ID 10 exists")
        .uponReceiving("a request to get a product via GraphQL")
          .path("/graphql")
          .method("POST")
          .body(query)
        .willRespondWith()
          .headers(Map.of("content-type", "application/json"))
          .status(200)
          .body(body)
        .toPact();
    }

  @PactTestFor(pactMethod = "getProduct")
  @Test
  public void testGetProduct(MockServer mockServer) throws IOException, URISyntaxException {
    Product product = new ProductClient().setUrl(mockServer.getUrl()).getProduct("10");

    assertThat(product.getId(), is("10"));
  }
}