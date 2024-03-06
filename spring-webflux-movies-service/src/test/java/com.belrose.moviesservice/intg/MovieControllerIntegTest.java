package com.belrose.moviesservice.intg;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.belrose.moviesservice.MoviesServiceApplication;
import com.belrose.moviesservice.domain.Movie;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = MoviesServiceApplication.class,webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084) //spin up httpserver in port 8084
@TestPropertySource(
    properties = {
        "restClient.moviesInfo.url.http://localhost:8084/v1/moviesinfo",
        "restClient.reviews.url.http://localhost:8084/v1/reviews"
    }
)
public class MovieControllerIntegTest {

  @Autowired
  WebTestClient webTestClient;

  @Test
  void retrieveMovieById(){

    //given
    var movieId ="abc";
    stubFor(get(urlEqualTo("/v1/moviesinfo"+"/"+movieId))
        .willReturn(aResponse()
        .withHeader("Content-Type","application/json")
        .withBodyFile("movieinfo.json")));

    stubFor(get(urlPathEqualTo("v1/reviews")).willReturn(aResponse()
        .withHeader("Content-Type","application/json")
        .withBodyFile("reviews.json")));

    //when
    webTestClient.get()
        .uri("/v1/movies/{id}", movieId)
        .exchange()
        .expectBody(Movie.class)
        .consumeWith(movieEntityExchangeResult -> {
          var movie = movieEntityExchangeResult.getResponseBody();
          assert Objects.requireNonNull(movie).getReviewList().size()==2;
          //assertEquals("Batman Begins",movie.getMovieInfo().getName());
        });

    //then

  }

}
