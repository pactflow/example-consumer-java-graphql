package com.example.products;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;

// uses the query string approach to fetch data
// see the alternative client ProductClientPost for the HTTP POST method
public class ProductClient {
  private String url;

  public ProductClient setUrl(String url) {
    this.url = url;

    return this;
  }

  public Product getProduct(String id) throws IOException, URISyntaxException {
    String query = "{\"query\":\"{\n  product(id: %s) {id \n name \n type }}\"}".formatted(id);

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