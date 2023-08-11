package ru.practicum;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
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
        StringBuilder url = new StringBuilder(serverUrl + "/stats?");
        for (String uri : uris) {
            url.append("&uris=").append(uri);
        }
        url.append("&unique=").append(unique);
        url.append("&start=").append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        url.append("&end=").append(end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        ResponseEntity<Object> response;
        try {
            response = restTemplate.exchange(url.toString(), HttpMethod.GET, null, Object.class);
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