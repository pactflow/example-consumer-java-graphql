package com.example.products;

import org.junit.jupiter.api.extension.ExtendWith;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
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

    // NOTE: GraphQL uses a JSON formatted payload to send queries over the wire
    //       You should familiarise yourself with the structure of this and consider
    //       creating a utility function to reduce boilerplate and aid readibility in your tests.
    //       A common cause of test failures with GraphQL is the fact that queries are
    //       contained within a "query" property in the JSON, and any formatting differences
    //       between the expectation (below) and how your API client actually sends a request 
    //       will result in a failure (the mock server will return an HTTP 500)
    final String query = """
      {
      "query": "{
        product(id: 10) {
          id
          name
          type
        }}
      "}
      """;

    return builder
      .given("a product with ID 10 exists")
      .uponReceiving("a request to get a product via GraphQL")
        .path("/graphql")
        .headers(Map.of("content-type", "application/json"))
        .method("POST")
        .body(query)
      .willRespondWith()
        .headers(Map.of("content-type", "application/json"))
        .status(200)
        .body(body)
      .toPact();
  }

  @PactTestFor(pactMethod = "getProduct", pactVersion = PactSpecVersion.V3)
  @Test
  public void testGetProduct(MockServer mockServer) throws IOException, URISyntaxException {
    Product product = new ProductClient().setUrl(mockServer.getUrl()).getProduct("10");

    assertThat(product.getId(), is("10"));
  }
}