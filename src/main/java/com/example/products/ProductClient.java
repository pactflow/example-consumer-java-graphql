package com.example.products;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;

public class ProductClient {
  private String url;

  public ProductClient setUrl(String url) {
    this.url = url;

    return this;
  }

  public Product getProduct(String id) throws IOException, URISyntaxException {
    final String query = """
      {
      "query": "{
        product(id: %s) {
          id
          name
          type
        }}
      "}
      """.formatted(id);    

    return (Product) Request.Post(this.url + "/graphql")
      .bodyString(query, ContentType.APPLICATION_JSON)
      .addHeader("Accept", "application/json")
      .execute().handleResponse(httpResponse -> {
        try {
          ObjectMapper mapper = new ObjectMapper();
          Response response = mapper.readValue(httpResponse.getEntity().getContent(), Response.class);
          return response.getData().getProduct();

        } catch (JsonMappingException e) {
          throw new IOException(e);
        }
      });
  }
}