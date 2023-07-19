package ru.practicum;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsClient {
    @Value("${client.url}")
    private String serverUrl;
    private final RestTemplate restTemplate;

    public StatsClient() {
        this.restTemplate = new RestTemplate();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.setRequestFactory(requestFactory);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        String uri = String.join(",", uris);

        HashMap<String, Object> params = new HashMap<>(Map.of("start", start,
                "end", end,
                "uris", uri,
                "unique", unique));

        ResponseEntity<Object> response;

        try {
            response = restTemplate.getForEntity(serverUrl + "/stats", Object.class, params);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto hit) {
        ResponseEntity<Object> response;
        try {
            response = restTemplate.postForEntity(serverUrl + "/hit", hit, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}